export interface CollectionTarget {
  id: number
  dataCode: string
  dataNameKo: string
  enabled: boolean
  scheduleType: string
  intervalValue: number
  lastExecutedAt: string | null
  nextExecutedAt: string | null
  latestStatus: string | null
}

export interface CollectionTargetListResponse {
  items: CollectionTarget[]
}

export interface UpdateCollectionTargetRequest {
  enabled: boolean
  scheduleType: string
  intervalValue: number
  executionTime?: string | null
}

export interface CollectionDashboardSummary {
  targetCount: number
  enabledTargetCount: number
  runningCount: number
  successCount: number
  failedCount: number
  latestCollectedAt: string | null
}

export interface CollectionJobSummary {
  id: number
  targetId: number
  targetName: string | null
  status: string
  triggerType: string
  startedAt: string
  finishedAt: string | null
  receivedCount: number | null
  savedCount: number | null
  duplicateCount: number | null
  errorCode: string | null
}

export interface CollectionJobListResponse {
  items: CollectionJobSummary[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

export interface CollectionJobDetail {
  id: number
  targetName: string | null
  status: string
  triggerType: string
  startedAt: string
  finishedAt: string | null
  receivedCount: number | null
  savedCount: number | null
  duplicateCount: number | null
  errorCode: string | null
  errorMessage: string | null
  retryable: boolean | null
  retryOfJobId: number | null
}

export interface CollectionJobsQuery {
  targetId?: number
  status?: string
  triggerType?: string
  page?: number
  size?: number
}

export interface ExecuteCollectionResponse {
  jobId: number
  status: string
}
