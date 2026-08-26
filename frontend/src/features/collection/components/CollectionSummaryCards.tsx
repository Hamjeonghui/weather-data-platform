import { useCollectionDashboardSummary } from '../hooks/useCollectionDashboardSummary'
import { getApiErrorMessage } from '../../../lib/apiClient'
import { formatDateTime } from '../utils/formatDateTime'

export function CollectionSummaryCards() {
  const { data, isLoading, isError, error } = useCollectionDashboardSummary()

  if (isLoading) {
    return <p>요약 정보를 불러오는 중...</p>
  }

  if (isError || !data) {
    return (
      <p role="alert" className="collection-error">
        {getApiErrorMessage(error)}
      </p>
    )
  }

  const cards = [
    { label: '전체 수집 대상', value: data.targetCount },
    { label: '활성 대상', value: data.enabledTargetCount },
    { label: '실행 중', value: data.runningCount },
    { label: '성공', value: data.successCount },
    { label: '실패', value: data.failedCount },
    { label: '최근 수집 완료', value: formatDateTime(data.latestCollectedAt) },
  ]

  return (
    <div className="collection-summary-cards">
      {cards.map((card) => (
        <div className="collection-summary-card" key={card.label}>
          <span className="collection-summary-card-label">{card.label}</span>
          <span className="collection-summary-card-value">{card.value}</span>
        </div>
      ))}
    </div>
  )
}
