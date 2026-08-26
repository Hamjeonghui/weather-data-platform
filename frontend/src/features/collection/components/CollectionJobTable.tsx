import { useState } from 'react'
import { useCollectionTargets } from '../hooks/useCollectionTargets'
import { useCollectionJobs } from '../hooks/useCollectionJobs'
import { getApiErrorMessage } from '../../../lib/apiClient'
import { formatDateTime } from '../utils/formatDateTime'

const STATUS_OPTIONS = ['RUNNING', 'SUCCESS', 'FAILED']
const TRIGGER_TYPE_OPTIONS = ['MANUAL', 'SCHEDULED']
const PAGE_SIZE = 10

interface CollectionJobTableProps {
  initialTargetId: number | null
  selectedJobId: number | null
  onSelectJob: (jobId: number) => void
}

export function CollectionJobTable({ initialTargetId, selectedJobId, onSelectJob }: CollectionJobTableProps) {
  const [targetId, setTargetId] = useState<number | ''>(initialTargetId ?? '')
  const [status, setStatus] = useState('')
  const [triggerType, setTriggerType] = useState('')
  const [page, setPage] = useState(0)

  const { data: targetsData } = useCollectionTargets()
  const { data, isLoading, isError, error } = useCollectionJobs({
    targetId: targetId === '' ? undefined : targetId,
    status: status === '' ? undefined : status,
    triggerType: triggerType === '' ? undefined : triggerType,
    page,
    size: PAGE_SIZE,
  })

  return (
    <div className="collection-job-table-wrapper">
      <div className="collection-job-filters">
        <label>
          수집 대상
          <select
            value={targetId}
            onChange={(event) => {
              setTargetId(event.target.value === '' ? '' : Number(event.target.value))
              setPage(0)
            }}
          >
            <option value="">전체</option>
            {targetsData?.items.map((target) => (
              <option key={target.id} value={target.id}>
                {target.dataNameKo}
              </option>
            ))}
          </select>
        </label>
        <label>
          상태
          <select
            value={status}
            onChange={(event) => {
              setStatus(event.target.value)
              setPage(0)
            }}
          >
            <option value="">전체</option>
            {STATUS_OPTIONS.map((option) => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
        </label>
        <label>
          트리거
          <select
            value={triggerType}
            onChange={(event) => {
              setTriggerType(event.target.value)
              setPage(0)
            }}
          >
            <option value="">전체</option>
            {TRIGGER_TYPE_OPTIONS.map((option) => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
        </label>
      </div>

      {isLoading && <p>수집 이력을 불러오는 중...</p>}
      {isError && (
        <p role="alert" className="collection-error">
          {getApiErrorMessage(error)}
        </p>
      )}

      {data && (
        <>
          <table className="collection-table">
            <thead>
              <tr>
                <th>번호</th>
                <th>수집 대상</th>
                <th>상태</th>
                <th>트리거</th>
                <th>시작</th>
                <th>종료</th>
                <th>저장 건수</th>
              </tr>
            </thead>
            <tbody>
              {data.items.map((job) => (
                <tr
                  key={job.id}
                  className={
                    job.id === selectedJobId ? 'collection-table-row-clickable collection-row-selected' : 'collection-table-row-clickable'
                  }
                  onClick={() => onSelectJob(job.id)}
                >
                  <td>{job.id}</td>
                  <td>{job.targetName ?? '-'}</td>
                  <td>
                    <span className={`collection-status collection-status-${job.status.toLowerCase()}`}>
                      {job.status}
                    </span>
                  </td>
                  <td>{job.triggerType}</td>
                  <td>{formatDateTime(job.startedAt)}</td>
                  <td>{formatDateTime(job.finishedAt)}</td>
                  <td>{job.savedCount ?? '-'}</td>
                </tr>
              ))}
              {data.items.length === 0 && (
                <tr>
                  <td colSpan={7}>조회된 수집 이력이 없습니다.</td>
                </tr>
              )}
            </tbody>
          </table>

          <div className="collection-pagination">
            <button type="button" onClick={() => setPage((prev) => prev - 1)} disabled={data.first}>
              이전
            </button>
            <span>
              {data.page + 1} / {Math.max(data.totalPages, 1)} 페이지 (총 {data.totalElements}건)
            </span>
            <button type="button" onClick={() => setPage((prev) => prev + 1)} disabled={data.last}>
              다음
            </button>
          </div>
        </>
      )}
    </div>
  )
}
