# INQACC Azure infrastructure

Artifact-only implementation of the human-selected Azure Container Apps,
Container Registry, Key Vault and Log Analytics/Application Insights platform
using Bicep (`docs/modernization/03a-architecture/target-architecture.md:5`).
Nothing has been provisioned. This is not a cutover or service-readiness claim.

## Scope

`main.bicep` targets an existing, environment-specific resource group. It creates
a spoke VNet, delegated Container Apps subnet, private endpoint subnet, an
internal zone-redundant workload-profiles environment, Premium ACR, dedicated
RBAC Key Vault, workload managed identity, workspace and Application Insights.
It adds peerings to an existing connectivity hub, links existing shared private
DNS zones, and connects the monitoring resources to an existing shared Azure
Monitor Private Link Scope (AMPLS). Existing hub/gateway/circuit, DNS zones and
AMPLS are referenced, not recreated. New child peerings, DNS links and scoped
resource links are intentional additive changes to those foundations.

The Container App is conditional on `deployApplication`, which defaults to
false in all environments (`infra/main.bicep:69`). There is no placeholder image
and no application created by the supplied parameter files. The checked-in
rewrite is a framework-independent core, not a running REST/Spring service
(`docs/modernization/03b-rewrite/INQACC/interface.md:360`).

No business database, replica, cache, CDC, DB2/MQ credentials or direct database
access is created. ADR-004 retains legacy data authority and API-mediated
coexistence, and explicitly prohibits a data move or new writer
(`docs/modernization/03a-architecture/target-architecture.md:131`). The NSG
permits TCP 443 to the supplied z/OS Connect destinations before denying all
other traffic to the supplied mainframe CIDRs, including DB2/MQ. This is not a
general internet egress firewall: required Container Apps platform traffic
retains Azure's default network rules. Network owners must supply the complete
mainframe CIDR inventory and approve the platform egress policy.

The application uses environment-wide private ingress. `external: true` on the
app means reachable from outside that app's environment through the **internal
VNet load balancer**, not public internet ingress. HTTPS is mandatory at the
ingress; HTTP probes target the local port 8080. No API gateway or routing
product is invented, and no existing `/api` route changes. Replacement
activation remains gated at the existing method/path/cohort seam
(`docs/modernization/03b-rewrite/INQACC/interface.md:334`).

## Existing foundation prerequisites

These are required site inputs, not assumed discovered Azure resources:

| Foundation | Required acceptance |
|---|---|
| Resource groups and subscriptions | Separate workload RG per environment; hub, shared DNS and AMPLS RGs already exist in the same Entra tenant. Register Microsoft.App, Network, ManagedIdentity, KeyVault, ContainerRegistry, OperationalInsights and Insights providers. |
| Connectivity hub | Existing VPN/ExpressRoute gateway, working site connection, gateway-transit capability, and permissions for both peerings. Site must advertise mainframe routes and accept return routes for each new spoke; peering alone does not establish a mainframe circuit. |
| CIDRs | Allocate distinct non-overlapping spoke ranges for each environment and against all hub/site networks. Apps subnet must be /23 or larger; private endpoint subnet must have spare addresses for ACR, vault and Monitor endpoints. |
| DNS | Reachable enterprise resolvers outside the mainframe-denied ranges; conditional forwarding to Azure DNS/Private Resolver for the zones below. Shared zones must already be linked to the resolver's hub VNet. Resolve the accepted private z/OS Connect hostname and its TLS certificate. |
| Azure Monitor | Existing shared AMPLS with PrivateOnly ingestion/query policy and all required shared zones; use one scope for the interconnected DNS domain. Owner must accept adding this workload and verify that endpoint DNS changes cannot disrupt other workloads. |
| Operators | Deployment identity needs resource creation and RBAC assignment rights in workload RG, peering rights on hub, private DNS link/record permissions and AMPLS scoped-resource permissions in shared RGs. Bicep creates no operator account, persistent CI access or broad administrator assignment. |

Existing shared zones, referenced by `modules/dns-links.bicep`:

```text
privatelink.azurecr.io
privatelink.vaultcore.azure.net
privatelink.monitor.azure.com
privatelink.oms.opinsights.azure.com
privatelink.ods.opinsights.azure.com
privatelink.agentsvc.azure-automation.net
privatelink.blob.core.windows.net
```

The dynamically assigned Container Apps domain gets a separate private zone,
apex/wildcard records and hub/spoke links after environment creation. Configure
enterprise forwarding for the emitted `internalDomain`. ACR and Key Vault
public network access is disabled; Monitor public ingestion/query is disabled.
Image publishing, vault population, telemetry queries and probes require a
private-network-connected operator/runner and working DNS. Azure-managed
diagnostic delivery uses the platform path; application telemetry uses AMPLS.

## Environment inputs

Use Bicep CLI 0.31+ (or a compatible newer Azure CLI Bicep installation).
`dev.bicepparam`, `staging.bicepparam` and `production.bicepparam` use the same
topology, SKU, region policy, resource security, retention and probe settings.
Only environment identity, replica scale and vault secret names differ.
Site-specific inputs are external, not guessed defaults:

| Environment variable | Value shape |
|---|---|
| `BANKZ_LOCATION` | Owner-accepted Azure region supporting zone-redundant Container Apps. |
| `BANKZ_HUB_SUBSCRIPTION_ID`, `BANKZ_HUB_RESOURCE_GROUP`, `BANKZ_HUB_VNET_NAME` | Existing connectivity hub identifiers. |
| `BANKZ_DNS_SUBSCRIPTION_ID`, `BANKZ_DNS_RESOURCE_GROUP` | Existing shared private DNS scope. |
| `BANKZ_MONITOR_SUBSCRIPTION_ID`, `BANKZ_MONITOR_RESOURCE_GROUP`, `BANKZ_MONITOR_PRIVATE_LINK_SCOPE_NAME` | Existing shared AMPLS scope. |
| `BANKZ_SPOKE_CIDR`, `BANKZ_APPS_SUBNET_CIDR`, `BANKZ_ENDPOINTS_SUBNET_CIDR` | Allocated IPv4 CIDRs for the selected environment. |
| `BANKZ_DNS_SERVERS_JSON` | JSON array of enterprise resolver IPv4 addresses. |
| `BANKZ_MAINFRAME_CIDRS_JSON` | JSON array of all mainframe destination IPv4 CIDRs. |
| `BANKZ_ZOS_CONNECT_CIDRS_JSON` | JSON array of allowed HTTPS destination IPv4 addresses/CIDRs, contained within the mainframe inventory. |

Missing variables cause parameter compilation to fail; no silent site defaults
are provided. Each environment uses its own RG and allocated CIDRs even when
sharing the connectivity/DNS/Monitor foundations. Scale is dev 1-3, staging
2-6, production 3-12 replicas. These are initial infrastructure sizing choices,
not accepted banking capacity or availability thresholds.

## Vault and image preparation gates

After a separately approved foundation provisioning step, a vault operator must
populate these named secrets through the private endpoint; this template
neither creates secrets nor outputs their contents:

| Environment | z/OS Connect HTTPS base URL | Accepted adapter credential | Application Insights connection string |
|---|---|---|---|
| dev | `zos-connect-base-url-dev` | `zos-connect-credential-dev` | `applicationinsights-connection-string-dev` |
| staging | `zos-connect-base-url-staging` | `zos-connect-credential-staging` | `applicationinsights-connection-string-staging` |
| production | `zos-connect-base-url-production` | `zos-connect-credential-production` | `applicationinsights-connection-string-production` |

Store the connection string belonging to the **generated environment's**
Application Insights resource. The connection string selects the ingestion
destination; Entra authentication still requires the workload identity's
Monitoring Metrics Publisher role. The identity has AcrPull on its own registry
and Key Vault Secrets User on its dedicated application vault, not a shared
enterprise vault. No registry admin password, telemetry shared key or inline
credential is used. Versionless vault references permit controlled rotation;
verify refresh and recycle behavior in a rehearsal before relying on it.

Credential format and identity enforcement remain owner-accepted application
adapter contracts, not an implied Basic/OAuth or mainframe user-mapping
decision. Accept TLS trust and hostname validation for z/OS Connect; do not
disable certificate validation or use plaintext URLs.

Before `deployApplication=true`, publish an approved Linux image to the generated
private ACR and provide `imageRepository` and `imageDigest` (`sha256:` plus 64
hex digits). Do not use a mutable tag; validate digest format and
`maxReplicas >= minReplicas` in release review. Build the Spring HTTP wrapper,
accepted data/identity adapters and instrumentation separately; this IaC task
does not rewrite them. The application image must:

- Implement both accepted REST projections and all INQACC module behavior;
  run `bash modern/inqacc/run-tests.sh` plus accepted HTTP, concurrency,
  identity, diagnostics and live-integration checks before enabling compute.
- Expose Actuator liveness/readiness on port 8080, honor the declared secret
  environment variables and `BANKZ_REPLACEMENT_ROUTES_ENABLED=false`, and
  enforce fail-closed access. An environment variable alone is not a routing
  or authorization implementation.
- Include and launch the supported Azure Monitor Application Insights Java
  agent in the image; use `APPLICATIONINSIGHTS_CONFIGURATION_CONTENT` with UAMI
  auth, plus the vault-sourced connection string. Test agent startup, private
  ingestion and redaction; Bicep does not install or attach a Java agent.
- Preserve single execution without retry/hedging/shadowing of legacy
  diagnostics or rollback effects. Accept the inquiry read-path/consistency
  contract before treating a provider-forwarding service as a replacement
  (`docs/modernization/03b-rewrite/INQACC/interface.md:344`).

Keep external strangler routing unchanged even after compute is enabled.
Readiness and an Azure revision's traffic weight do not authorize banking
cutover; 100% here is only traffic **already addressed to this private app**.

## Offline verification and limitations

Local-only commands for a capable validation environment (not executed here):

```bash
az bicep build --file infra/main.bicep --stdout > /dev/null
az bicep build-params --file infra/dev.bicepparam --stdout > /dev/null
az bicep build-params --file infra/staging.bicepparam --stdout > /dev/null
az bicep build-params --file infra/production.bicepparam --stdout > /dev/null
checkov -d infra/
```

Set the site-input variables before parameter compilation. Resolve all compiler
errors and review security findings before any provisioning; no suppressions
are supplied. This session has no command-execution tool, Bicep compiler,
schema-fetch tool or security-scanner tool exposed. Files were statically
reviewed, but **compiler validation and Checkov are unverified**. No build,
scan, Azure validation, apply, deployment, network probe or mainframe command
was run. Live health/telemetry/connectivity acceptance remains pending.

Static self-review: module paths, supplied/required parameters and consumed
outputs aligned; dynamic environment DNS isolated in a dependent module;
hub gateway-transit peering precedes spoke useRemoteGateways; no secret values
or secret outputs; private endpoints, workload RBAC and vault protection
present; legacy sources and existing mainframe workflows untouched. These
checks do not substitute for compilation, resource-provider validation,
tenant permission checks or an approved deployment.

| Generation self-check | Status |
|---|---|
| Bicep and environment-parameter compilation | UNVERIFIED: execution tool unavailable; no command output or exit status. |
| Checkov with no unresolved high/critical findings | UNVERIFIED: scanner execution unavailable; no findings report. |
| Secure-by-default artifact review | PASS (static): internal compute ingress, private service endpoints, no inline secrets, workload-scoped RBAC, vault soft-delete/purge protection, HTTPS ingress and required backend TLS validation. |
| Existing foundations wired without recreation | PASS (static): hub, shared DNS and AMPLS referenced; only additive child wiring emitted. Site existence/connectivity unverified. |
| IaC files under `infra/`; legacy sources untouched | PASS: all emitted Bicep/parameter files are under `infra/`; no edits to `src/`, `.setup/` or existing workflows. |
