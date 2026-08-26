import { useQuery } from '@tanstack/react-query'
import { getCollectionDashboardSummary } from '../api/collectionApi'

export function useCollectionDashboardSummary() {
  return useQuery({
    queryKey: ['collection', 'dashboard-summary'],
    queryFn: getCollectionDashboardSummary,
  })
}
