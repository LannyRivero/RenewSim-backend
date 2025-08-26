# Login Rate Limiting — Properties & Behavior

This service enforces rate limiting on `/auth/login` to mitigate brute force attacks.

## Properties

| Property                                      | Type    | Default | Description |
|-----------------------------------------------|---------|---------|-------------|
| `security.rate-limiting.strategy`             | enum    | `IP`    | `IP` or `IP_USER` (reads username when available without consuming the request body). |
| `security.rate-limiting.window-seconds`       | int     | `60`    | Time window in seconds. |
| `security.rate-limiting.max-attempts`         | int     | `5`     | Allowed attempts within the window. |
| `security.rate-limiting.block-seconds`        | int     | `300`   | Block duration after exceeding the threshold. |

## Behavior

- Exceeding `max-attempts` within `window-seconds` triggers **HTTP 429 Too Many Requests**.
- Response includes `Retry-After: <block-seconds>` header.
- The service adds cache controls on error responses:  
  `Cache-Control: no-store`, `Pragma: no-cache`, `Expires: 0`.
- Strategy `IP_USER` combines client IP + username hash to reduce false positives on shared IPs.

## Error Schema

```json
{
  "timestamp": "2025-08-26T09:15:00Z",
  "status": 429,
  "error": "TooManyRequests",
  "message": "Login attempts exceeded. Try again later.",
  "path": "/api/v1/auth/login",
  "details": { "retryAfterSeconds": 300 }
}
