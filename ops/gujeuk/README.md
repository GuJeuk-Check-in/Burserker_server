# Gujeuk AWS EC2 Docker Deployment

현재 자동 배포는 `.github/workflows/gujeuk-ci-cd.yml`을 사용합니다. 아래 문서는 최초 EC2 설치/수동 기동 절차의 이력이며, 법동을 포함한 현재 운영·스테이징 구조는 `../beopdong/README.md`를 우선 확인합니다.

Target:

- Region: Seoul `ap-northeast-2`
- One EC2 instance
- Docker Compose prod and stag on the same host
- Docker MySQL and Docker Redis
- New Cloudflare records:
  - `aws-api.oijwef098234.com` -> EC2 Caddy -> prod port `8080`
  - `aws-stag.oijwef098234.com` -> EC2 Caddy -> stag port `8081`
- No CI/CD in this phase
- No zero-downtime cutover in this phase

## 1. Create EC2

Recommended first instance:

- Ubuntu Server 24.04 LTS
- `t3.small` minimum, `t3.medium` safer
- Storage: 30GB gp3 minimum
- Security group inbound:
  - SSH `22` from your IP only
  - HTTP `80` from `0.0.0.0/0`
  - Do not expose prod `8080` or stag `8081` publicly

## 2. Bootstrap EC2

Copy and run:

```bash
bash ops/gujeuk/bootstrap-ec2.sh
```

Reconnect after the script finishes:

```bash
exit
ssh <ec2-user>@<ec2-public-ip>
```

## 3. Prepare Files On EC2

```bash
mkdir -p ~/gujeuk-aws/prod ~/gujeuk-aws/stag
```

Copy:

```text
ops/gujeuk/docker-compose.aws.yml -> ~/gujeuk-aws/prod/docker-compose.yml
ops/gujeuk/docker-compose.aws.yml -> ~/gujeuk-aws/stag/docker-compose.yml
ops/gujeuk/prod.env.example       -> ~/gujeuk-aws/prod/.env
ops/gujeuk/stag.env.example       -> ~/gujeuk-aws/stag/.env
ops/gujeuk/docker-compose.proxy.yml -> ~/gujeuk-aws/proxy/docker-compose.yml
ops/gujeuk/Caddyfile                -> ~/gujeuk-aws/proxy/Caddyfile
```

Edit `.env` files and replace every `change-me-*` value.

## 4. Login To GHCR

Use a GitHub token with `read:packages`.

```bash
echo '<github-token>' | docker login ghcr.io -u '<github-username>' --password-stdin
```

## 5. Start Prod

```bash
cd ~/gujeuk-aws/prod
docker compose --env-file .env pull
docker compose --env-file .env up -d
docker compose --env-file .env ps
curl -i http://localhost:8080/common/health/ready
```

## 6. Start Stag

```bash
cd ~/gujeuk-aws/stag
docker compose --env-file .env pull
docker compose --env-file .env up -d
docker compose --env-file .env ps
curl -i http://localhost:8081/common/health/ready
```

## 7. Cloudflare DNS

Start the host-routing proxy after the prod and stag stacks are healthy:

```bash
cd ~/gujeuk-aws/proxy
docker compose up -d
```

Create proxied records in the `oijwef098234.com` zone:

```text
Type: A
Name: aws-api
Target: <EC2 public IPv4>
Proxy status: Proxied

Type: A
Name: aws-stag
Target: <EC2 public IPv4>
Proxy status: Proxied
```

Then verify:

```bash
curl -i https://aws-api.oijwef098234.com/common/health/ready
curl -i https://aws-stag.oijwef098234.com/common/health/ready
```

If Cloudflare returns 521/522, check EC2 security group and host firewall.

## 8. Data

Production and staging MySQL/Redis data were migrated on 2026-07-20. The legacy
`api.taisu.site` records still target the home-server tunnel; clients using the
`aws-*` hostnames reach AWS directly. Zero-downtime deployment is not configured.

Flyway migrations must remain idempotent for both fresh databases and restored
legacy schemas.
