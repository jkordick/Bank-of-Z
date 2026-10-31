@description('Azure region.')
param location string
@description('Environment-specific resource prefix.')
param prefix string
@description('Resource tags.')
param tags object
@description('Internal Container Apps environment resource ID.')
param environmentId string
@description('User-assigned workload identity resource ID.')
param identityId string
@description('User-assigned workload identity client UUID for Entra-authenticated telemetry.')
param identityClientId string
@description('Private ACR login server.')
param registryServer string
@description('Key Vault URI; no secret values are passed to the deployment.')
param vaultUri string
@description('Key Vault secret name for the z/OS Connect HTTPS URL.')
param zosConnectUrlSecretName string
@description('Key Vault secret name for the accepted z/OS Connect credential.')
param zosConnectCredentialSecretName string
@description('Key Vault secret name for the Application Insights connection string.')
param applicationInsightsSecretName string
@description('Approved instrumented Spring Boot image repository in ACR.')
param imageRepository string
@description('Immutable sha256 digest, including sha256: prefix.')
@minLength(71)
@maxLength(71)
param imageDigest string
@description('Minimum replicas.')
@minValue(1)
param minReplicas int
@description('Maximum replicas; must be at least the minimum.')
@minValue(1)
param maxReplicas int

resource app 'Microsoft.App/containerApps@2024-03-01' = {
  name: '${prefix}-inqacc'
  location: location
  tags: tags
  identity: {
    type: 'UserAssigned'
    userAssignedIdentities: { '${identityId}': {} }
  }
  properties: {
    managedEnvironmentId: environmentId
    workloadProfileName: 'Consumption'
    configuration: {
      activeRevisionsMode: 'Single'
      ingress: {
        external: true
        targetPort: 8080
        transport: 'http'
        allowInsecure: false
        traffic: [
          {
            latestRevision: true
            weight: 100
          }
        ]
      }
      registries: [
        {
          server: registryServer
          identity: identityId
        }
      ]
      secrets: [
        {
          name: 'zos-connect-url'
          keyVaultUrl: '${vaultUri}secrets/${zosConnectUrlSecretName}'
          identity: identityId
        }
        {
          name: 'zos-connect-credential'
          keyVaultUrl: '${vaultUri}secrets/${zosConnectCredentialSecretName}'
          identity: identityId
        }
        {
          name: 'application-insights'
          keyVaultUrl: '${vaultUri}secrets/${applicationInsightsSecretName}'
          identity: identityId
        }
      ]
    }
    template: {
      containers: [
        {
          name: 'inqacc'
          image: '${registryServer}/${imageRepository}@${imageDigest}'
          resources: {
            cpu: json('0.5')
            memory: '1Gi'
          }
          env: [
            {
              name: 'SERVER_PORT'
              value: '8080'
            }
            {
              name: 'MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE'
              value: 'health'
            }
            {
              name: 'MANAGEMENT_ENDPOINT_HEALTH_PROBES_ENABLED'
              value: 'true'
            }
            {
              name: 'MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS'
              value: 'never'
            }
            {
              name: 'APPLICATIONINSIGHTS_CONNECTION_STRING'
              secretRef: 'application-insights'
            }
            {
              name: 'AZURE_CLIENT_ID'
              value: identityClientId
            }
            {
              name: 'APPLICATIONINSIGHTS_CONFIGURATION_CONTENT'
              value: string({
                authentication: {
                  enabled: true
                  type: 'UAMI'
                  clientId: identityClientId
                }
                role: { name: '${prefix}-inqacc' }
                sampling: { percentage: 100 }
              })
            }
            {
              name: 'ZOS_CONNECT_BASE_URL'
              secretRef: 'zos-connect-url'
            }
            {
              name: 'ZOS_CONNECT_CREDENTIAL'
              secretRef: 'zos-connect-credential'
            }
            {
              name: 'BANKZ_REPLACEMENT_ROUTES_ENABLED'
              value: 'false'
            }
          ]
          probes: [
            {
              type: 'Startup'
              httpGet: {
                path: '/actuator/health/liveness'
                port: 8080
                scheme: 'HTTP'
              }
              periodSeconds: 10
              timeoutSeconds: 5
              failureThreshold: 30
            }
            {
              type: 'Liveness'
              httpGet: {
                path: '/actuator/health/liveness'
                port: 8080
                scheme: 'HTTP'
              }
              periodSeconds: 10
              timeoutSeconds: 5
              failureThreshold: 3
            }
            {
              type: 'Readiness'
              httpGet: {
                path: '/actuator/health/readiness'
                port: 8080
                scheme: 'HTTP'
              }
              periodSeconds: 10
              timeoutSeconds: 5
              failureThreshold: 3
            }
          ]
        }
      ]
      scale: {
        minReplicas: minReplicas
        maxReplicas: maxReplicas
        rules: [
          {
            name: 'http-concurrency'
            http: { metadata: { concurrentRequests: '20' } }
          }
        ]
      }
    }
  }
}
