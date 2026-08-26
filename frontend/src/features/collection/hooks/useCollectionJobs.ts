import { useQuery } from '@tanstack/react-query'
import { getCollectionJobs } from '../api/collectionApi'
import type { CollectionJobsQuery } from '../../../types/collection'

export function useCollectionJobs(query: CollectionJobsQuery) {
  return useQuery({
    queryKey: ['collection', 'jobs', query],
    queryFn: () => getCollectionJobs(query),
  })
}
