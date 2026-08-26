import { useQuery } from '@tanstack/react-query'
import { getCollectionJobDetail } from '../api/collectionApi'

export function useCollectionJobDetail(jobId: number | null) {
  return useQuery({
    queryKey: ['collection', 'jobs', 'detail', jobId],
    queryFn: () => getCollectionJobDetail(jobId as number),
    enabled: jobId !== null,
  })
}
