@description('Existing shared Azure Monitor Private Link Scope name.')
param privateLinkScopeName string
@description('Unique prefix for additive scoped-resource links.')
param linkPrefix string
@description('New workspace resource ID.')
param workspaceId string
@description('New Application Insights resource ID.')
param applicationInsightsId string

resource privateLinkScope 'Microsoft.Insights/privateLinkScopes@2021-07-01-preview' existing = {
  name: privateLinkScopeName
}

resource workspaceLink 'Microsoft.Insights/privateLinkScopes/scopedResources@2021-07-01-preview' = {
  parent: privateLinkScope
  name: '${linkPrefix}-workspace'
  properties: { linkedResourceId: workspaceId }
}

resource insightsLink 'Microsoft.Insights/privateLinkScopes/scopedResources@2021-07-01-preview' = {
  parent: privateLinkScope
  name: '${linkPrefix}-insights'
  properties: { linkedResourceId: applicationInsightsId }
}
