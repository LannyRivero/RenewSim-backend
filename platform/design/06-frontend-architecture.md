# Arquitectura Frontend — RenewSim

## Stack

| Tecnología | Versión | Propósito |
|-----------|---------|-----------|
| React | 18 | UI framework |
| TypeScript | 5 (strict) | Tipado estático |
| Vite | 5 | Bundler + HMR |
| Tailwind CSS | 3 | Estilos utilitarios |
| shadcn/ui | latest | Componentes base accesibles |
| Zustand | 4 | Estado global del cliente |
| TanStack Query | 5 | Estado del servidor + caché |
| React Hook Form | 7 | Formularios |
| Zod | 3 | Validación + inferencia de tipos |
| Axios | 1 | HTTP client |
| Recharts | 2 | Gráficos |
| Leaflet + react-leaflet | 1.9 / 4 | Mapas interactivos |
| React Router | 6 | Enrutamiento |
| react-markdown + rehype-sanitize | — | Renderizado seguro de Markdown |

---

## Estructura de Carpetas

```
src/
├── pages/
│   ├── LoginPage.tsx              ← Paso 1 + Paso 2 OTP (sin recarga)
│   ├── DashboardPage.tsx          ← Métricas agregadas + mapa + tabla
│   ├── SimulationCreatePage.tsx   ← Formulario de creación
│   ├── SimulationDetailPage.tsx   ← Resultados + gráficos + acciones
│   ├── SimulationComparePage.tsx  ← Tabla comparativa + radar chart
│   ├── SharedSimulationPage.tsx   ← Vista pública (sin auth)
│   └── NotFoundPage.tsx
├── components/
│   ├── auth/
│   │   ├── CredentialsForm.tsx    ← Paso 1: email + password
│   │   └── OtpForm.tsx            ← Paso 2: código OTP + contador
│   ├── simulation/
│   │   ├── SimulationForm.tsx     ← React Hook Form + Zod
│   │   ├── SimulationResults.tsx  ← Tarjetas de métricas
│   │   ├── ComparisonTable.tsx    ← Tabla lado a lado
│   │   ├── EnergyChart.tsx        ← Recharts área mensual
│   │   ├── CashFlowChart.tsx      ← Recharts líneas + payback
│   │   └── RadarChart.tsx         ← Recharts radar comparación
│   ├── map/
│   │   └── MapView.tsx            ← Leaflet + marcadores + popups
│   ├── ai/
│   │   └── ChatWidget.tsx         ← Widget flotante IA
│   └── shared/
│       ├── ProtectedRoute.tsx     ← Redirige a /login si no autenticado
│       ├── SkeletonCard.tsx       ← Loading states
│       └── ErrorBoundary.tsx      ← Captura errores de render
├── hooks/
│   ├── useSimulations.ts          ← TanStack Query hooks
│   ├── useTechnologies.ts
│   ├── useAuth.ts
│   └── useChat.ts
├── stores/
│   ├── authStore.ts               ← Zustand: accessToken en memoria
│   └── uiStore.ts                 ← Zustand: locale, chat abierto
├── services/
│   ├── httpClient.ts              ← Axios + interceptores JWT
│   ├── simulationService.ts
│   ├── authService.ts
│   ├── technologyService.ts
│   └── aiService.ts
└── types/
    ├── simulation.types.ts
    ├── auth.types.ts
    └── api.types.ts
```

---

## Rutas (React Router v6)

```tsx
// App.tsx
<Routes>
  <Route path="/login" element={<LoginPage />} />
  <Route path="/shared/:token" element={<SharedSimulationPage />} />
  <Route element={<ProtectedRoute />}>
    <Route path="/" element={<DashboardPage />} />
    <Route path="/simulations/new" element={<SimulationCreatePage />} />
    <Route path="/simulations/:id" element={<SimulationDetailPage />} />
    <Route path="/simulations/compare" element={<SimulationComparePage />} />
  </Route>
  <Route path="*" element={<NotFoundPage />} />
</Routes>
```

---

## Stores Zustand

```typescript
// stores/authStore.ts
interface AuthState {
  accessToken: string | null;       // En memoria, nunca en localStorage
  user: UserProfile | null;
  isAuthenticated: boolean;
  setTokens: (accessToken: string, user: UserProfile) => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  user: null,
  isAuthenticated: false,
  setTokens: (accessToken, user) =>
    set({ accessToken, user, isAuthenticated: true }),
  clearAuth: () =>
    set({ accessToken: null, user: null, isAuthenticated: false }),
}));

// stores/uiStore.ts
interface UIState {
  locale: 'es' | 'en';
  isChatOpen: boolean;
  setLocale: (locale: 'es' | 'en') => void;
  toggleChat: () => void;
}

export const useUIStore = create<UIState>((set) => ({
  locale: 'es',
  isChatOpen: false,
  setLocale: (locale) => set({ locale }),
  toggleChat: () => set((state) => ({ isChatOpen: !state.isChatOpen })),
}));
```

---

## Cliente HTTP con Interceptores JWT y Refresh Automático

```typescript
// services/httpClient.ts
const httpClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  withCredentials: true, // Envía cookie HttpOnly con refreshToken
});

// Inyecta accessToken en cada request
httpClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Maneja 401 → refresh automático
let isRefreshing = false;
let failedQueue: Array<{ resolve: (token: string) => void; reject: (err: unknown) => void }> = [];

const processQueue = (error: unknown, token: string | null) => {
  failedQueue.forEach((p) => (error ? p.reject(error) : p.resolve(token!)));
  failedQueue = [];
};

httpClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) =>
          failedQueue.push({ resolve, reject })
        ).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return httpClient(originalRequest);
        });
      }
      originalRequest._retry = true;
      isRefreshing = true;
      try {
        const { data } = await axios.post(
          `${import.meta.env.VITE_API_BASE_URL}/auth/refresh`,
          {},
          { withCredentials: true }
        );
        useAuthStore.getState().setTokens(data.accessToken, data.user);
        processQueue(null, data.accessToken);
        originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
        return httpClient(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        useAuthStore.getState().clearAuth();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }
    return Promise.reject(error);
  }
);

export default httpClient;
```

---

## Validación de Formularios (Zod)

```typescript
// Esquema que refleja exactamente las reglas de negocio del backend
export const simulationSchema = z.object({
  name: z.string().min(1, 'El nombre es obligatorio').max(255),
  technologyId: z.number({ required_error: 'Selecciona una tecnología' }).positive(),
  location: z.object({
    latitude: z.number().min(-90, 'Latitud inválida').max(90, 'Latitud inválida'),
    longitude: z.number().min(-180, 'Longitud inválida').max(180, 'Longitud inválida'),
  }),
  capacityKw: z
    .number()
    .min(1, 'Capacidad mínima: 1 kW')
    .max(10000, 'Capacidad máxima: 10.000 kW'),
  initialInvestment: z.number().positive('La inversión debe ser mayor a 0'),
  electricityTariff: z.number().positive('La tarifa debe ser mayor a 0'),
  currentConsumptionKwhYear: z.number().positive().optional(),
  climateData: z.object({
    avgSolarIrradiation: z.number().min(0).optional(),
    avgWindSpeed: z.number().min(0).optional(),
    avgTemperature: z.number().optional(),
  }).optional(),
});

export type SimulationFormData = z.infer<typeof simulationSchema>;
```

---

## Componente ChatWidget

```tsx
// components/ai/ChatWidget.tsx
export function ChatWidget() {
  const { isChatOpen, toggleChat } = useUIStore();
  const { messages, sendMessage, isLoading } = useChat();

  return (
    <>
      {/* Botón flotante */}
      <button
        onClick={toggleChat}
        className="fixed bottom-4 right-4 z-50 rounded-full bg-primary p-4 shadow-lg"
        aria-label="Abrir asistente IA"
      >
        <MessageCircle className="h-6 w-6 text-white" />
      </button>

      {/* Panel de chat */}
      {isChatOpen && (
        <div className="fixed bottom-20 right-4 z-50 flex h-96 w-80 flex-col rounded-xl border bg-background shadow-xl">
          <div className="flex-1 overflow-y-auto p-4 space-y-3">
            {messages.length === 0 && <QuickSuggestions onSelect={sendMessage} />}
            {messages.map((msg) => (
              <ChatBubble key={msg.id} message={msg} />
            ))}
            {isLoading && <TypingIndicator />}
          </div>
          <ChatInput onSend={sendMessage} disabled={isLoading} />
        </div>
      )}
    </>
  );
}

// Renderizado seguro de Markdown
function ChatBubble({ message }: { message: ChatMessage }) {
  return (
    <div className={cn('rounded-lg p-3', message.role === 'USER' ? 'bg-primary/10 ml-8' : 'bg-muted mr-8')}>
      <ReactMarkdown rehypePlugins={[rehypeSanitize]}>
        {message.content}
      </ReactMarkdown>
    </div>
  );
}
```
