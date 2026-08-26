import { useCollectionJobDetail } from '../hooks/useCollectionJobDetail'
import { getApiErrorMessage } from '../../../lib/apiClient'
import { formatDateTime } from '../utils/formatDateTime'

interface CollectionJobDetailPanelProps {
  jobId: number | null
  onClose: () => void
}

export function CollectionJobDetailPanel({ jobId, onClose }: CollectionJobDetailPanelProps) {
  const { data, isLoading, isError, error } = useCollectionJobDetail(jobId)

  if (jobId === null) {
    return null
  }

  return (
    <aside className="collection-job-detail-panel">
      <div className="collection-job-detail-header">
        <h2>수집 이력 상세 (#{jobId})</h2>
        <button type="button" onClick={onClose}>
          닫기
        </button>
      </div>

      {isLoading && <p>불러오는 중...</p>}
      {isError && (
        <p role="alert" className="collection-error">
          {getApiErrorMessage(error)}
        </p>
      )}

      {data && (
        <dl className="collection-job-detail-list">
          <dt>수집 대상</dt>
          <dd>{data.targetName ?? '-'}</dd>

          <dt>상태</dt>
          <dd>
            <span className={`collection-status collection-status-${data.status.toLowerCase()}`}>
              {data.status}
            </span>
          </dd>

          <dt>트리거</dt>
          <dd>{data.triggerType}</dd>

          <dt>시작 / 종료</dt>
          <dd>
            {formatDateTime(data.startedAt)} ~ {formatDateTime(data.finishedAt)}
          </dd>

          <dt>수집 / 저장 / 중복 건수</dt>
          <dd>
            {data.receivedCount ?? '알 수 없음'} / {data.savedCount ?? '알 수 없음'} /{' '}
            {data.duplicateCount ?? '알 수 없음'}
          </dd>

          {data.errorCode && (
            <>
              <dt>오류 코드</dt>
              <dd>{data.errorCode}</dd>

              <dt>오류 메시지</dt>
              <dd>{data.errorMessage ?? '-'}</dd>

              <dt>재시도 가능 여부</dt>
              <dd>{data.retryable === null ? '-' : data.retryable ? '가능' : '불가'}</dd>
            </>
          )}

          <dt>재시도 원본 작업</dt>
          <dd>{data.retryOfJobId ?? '-'}</dd>
        </dl>
      )}
    </aside>
  )
}
