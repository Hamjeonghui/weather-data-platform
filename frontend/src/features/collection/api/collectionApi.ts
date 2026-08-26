import { apiClient } from '../../../lib/apiClient'
import type {
  CollectionDashboardSummary,
  CollectionJobDetail,
  CollectionJobListResponse,
  CollectionJobsQuery,
  CollectionTarget,
  CollectionTargetListResponse,
  ExecuteCollectionResponse,
  UpdateCollectionTargetRequest,
} from '../../../types/collection'

export async function getCollectionTargets(): Promise<CollectionTargetListResponse> {
  const { data } = await apiClient.get<CollectionTargetListResponse>('/api/admin/collection-targets')
  return data
}

export async function updateCollectionTarget(
  targetId: number,
  request: UpdateCollectionTargetRequest,
): Promise<CollectionTarget> {
  const { data } = await apiClient.patch<CollectionTarget>(
    `/api/admin/collection-targets/${targetId}`,
    request,
  )
  return data
}

export async function executeCollection(targetId: number): Promise<ExecuteCollectionResponse> {
  const { data } = await apiClient.post<ExecuteCollectionResponse>(
    `/api/admin/collection-targets/${targetId}/execute`,
  )
  return data
}

export async function getCollectionDashboardSummary(): Promise<CollectionDashboardSummary> {
  const { data } = await apiClient.get<CollectionDashboardSummary>('/api/admin/collection-dashboard/summary')
  return data
}

export async function getCollectionJobs(query: CollectionJobsQuery): Promise<CollectionJobListResponse> {
  const { data } = await apiClient.get<CollectionJobListResponse>('/api/admin/collection-jobs', {
    params: query,
  })
  return data
}

export async function getCollectionJobDetail(jobId: number): Promise<CollectionJobDetail> {
  const { data } = await apiClient.get<CollectionJobDetail>(`/api/admin/collection-jobs/${jobId}`)
  return data
}
