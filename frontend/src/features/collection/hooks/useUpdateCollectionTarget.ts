import { useMutation, useQueryClient } from '@tanstack/react-query'
import { updateCollectionTarget } from '../api/collectionApi'
import type { UpdateCollectionTargetRequest } from '../../../types/collection'

interface UpdateCollectionTargetVariables {
  targetId: number
  request: UpdateCollectionTargetRequest
}

export function useUpdateCollectionTarget() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ targetId, request }: UpdateCollectionTargetVariables) =>
      updateCollectionTarget(targetId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['collection', 'targets'] })
    },
  })
}
