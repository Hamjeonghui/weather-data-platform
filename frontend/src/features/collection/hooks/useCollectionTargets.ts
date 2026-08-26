import { useQuery } from '@tanstack/react-query'
import { getCollectionTargets } from '../api/collectionApi'

export function useCollectionTargets() {
  return useQuery({
    queryKey: ['collection', 'targets'],
    queryFn: getCollectionTargets,
  })
}
