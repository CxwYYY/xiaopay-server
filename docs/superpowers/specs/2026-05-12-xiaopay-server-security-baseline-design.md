# xiaopay-server security baseline design

## Goal

Harden the current XiaoPay backend for a small public-facing deployment where only two trusted admins use the management console, while merchant and agent APIs remain machine-to-machine.

## Scope

- Protect `/api/admin/**` with Sa-Token and stricter token defaults.
- Reduce brute-force risk on admin login with Redis-backed failure counters.
- Prevent remote takeover of a fresh database by requiring a setup token for admin initialization when the request is not local.
- Require HMAC signature checks for all merchant order endpoints, including status query and close.
- Prevent short-window replay of signed app and agent requests by storing nonce keys in Redis for the signature window.
- Add strict CORS allowlist and basic security response headers.
- Add admin password change so test credentials do not need to stay permanent.

## Non-goals

- Full RBAC and multi-role permissions.
- Enterprise audit dashboard.
- WAF, CDN, Nginx, TLS certificate automation, or OS firewall setup.

## Behavior

- Admin login records failures by username and client IP. Too many failures in a short window returns a lock error. Successful login clears the counters.
- Admin initialization checks `X-XiaoPay-Setup-Token` when `xiaopay.security.setup-token` is configured. If no setup token is configured, initialization is allowed only from loopback clients.
- Signed requests reject blank nonce values and duplicate nonce values for the same identity inside the configured signature window.
- Admin CORS origins are configured by `xiaopay.security.allowed-origins`; credentials are allowed only for those origins.
- Security headers are added to every response: no sniffing, no referrer leakage, frame deny, and a conservative permissions policy.

## Testing

- Unit tests cover login lock/clear behavior, setup token validation, nonce replay rejection, and password change validation.
- Existing Maven tests remain the full regression gate.
