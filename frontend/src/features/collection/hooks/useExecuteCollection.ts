import { useMutation, useQueryClient } from '@tanstack/react-query'
import { executeCollection } from '../api/collectionApi'

export function useExecuteCollection() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (targetId: number) => executeCollection(targetId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['collection', 'targets'] })
      queryClient.invalidateQueries({ queryKey: ['collection', 'dashboard-summary'] })
      queryClient.invalidateQueries({ queryKey: ['collection', 'jobs'] })
    },
  })
}
