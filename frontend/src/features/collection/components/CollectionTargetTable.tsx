import { useState } from 'react'
import type { ChangeEvent } from 'react'
import { useCollectionTargets } from '../hooks/useCollectionTargets'
import { useExecuteCollection } from '../hooks/useExecuteCollection'
import { useUpdateCollectionTarget } from '../hooks/useUpdateCollectionTarget'
import { getApiErrorMessage } from '../../../lib/apiClient'
import { formatDateTime } from '../utils/formatDateTime'
import type { CollectionTarget } from '../../../types/collection'

const SCHEDULE_TYPES = ['MINUTE', 'HOUR', 'DAY'] as const

interface CollectionTargetTableProps {
  onSelectTarget: (targetId: number) => void
}

export function CollectionTargetTable({ onSelectTarget }: CollectionTargetTableProps) {
  const { data, isLoading, isError, error } = useCollectionTargets()

  if (isLoading) {
    return <p>수집 대상을 불러오는 중...</p>
  }

  if (isError || !data) {
    return (
      <p role="alert" className="collection-error">
        {getApiErrorMessage(error)}
      </p>
    )
  }

  return (
    <table className="collection-table">
      <thead>
        <tr>
          <th>수집 대상</th>
          <th>활성화</th>
          <th>수행 단위 / 주기</th>
          <th>최근 실행</th>
          <th>다음 실행 예정</th>
          <th>최근 상태</th>
          <th>작업</th>
        </tr>
      </thead>
      <tbody>
        {data.items.map((target) => (
          <CollectionTargetRow key={target.id} target={target} onSelectTarget={onSelectTarget} />
        ))}
      </tbody>
    </table>
  )
}

interface CollectionTargetRowProps {
  target: CollectionTarget
  onSelectTarget: (targetId: number) => void
}

function CollectionTargetRow({ target, onSelectTarget }: CollectionTargetRowProps) {
  const [isEditing, setIsEditing] = useState(false)
  const [enabled, setEnabled] = useState(target.enabled)
  const [scheduleType, setScheduleType] = useState(target.scheduleType)
  const [intervalValue, setIntervalValue] = useState(target.intervalValue)

  const executeMutation = useExecuteCollection()
  const updateMutation = useUpdateCollectionTarget()

  function startEditing() {
    setEnabled(target.enabled)
    setScheduleType(target.scheduleType)
    setIntervalValue(target.intervalValue)
    updateMutation.reset()
    setIsEditing(true)
  }

  function handleSave() {
    updateMutation.mutate(
      { targetId: target.id, request: { enabled, scheduleType, intervalValue } },
      { onSuccess: () => setIsEditing(false) },
    )
  }

  function handleIntervalChange(event: ChangeEvent<HTMLInputElement>) {
    setIntervalValue(Number(event.target.value))
  }

  if (isEditing) {
    return (
      <tr>
        <td>{target.dataNameKo}</td>
        <td>
          <input
            type="checkbox"
            checked={enabled}
            onChange={(event) => setEnabled(event.target.checked)}
            aria-label={`${target.dataNameKo} 활성화 여부`}
          />
        </td>
        <td className="collection-inline-edit">
          <select value={scheduleType} onChange={(event) => setScheduleType(event.target.value)}>
            {SCHEDULE_TYPES.map((type) => (
              <option key={type} value={type}>
                {type}
              </option>
            ))}
          </select>
          <input type="number" min={1} value={intervalValue} onChange={handleIntervalChange} />
        </td>
        <td>{formatDateTime(target.lastExecutedAt)}</td>
        <td>{formatDateTime(target.nextExecutedAt)}</td>
        <td>{target.latestStatus ?? '-'}</td>
        <td className="collection-row-actions">
          <button type="button" onClick={handleSave} disabled={updateMutation.isPending}>
            {updateMutation.isPending ? '저장 중...' : '저장'}
          </button>
          <button type="button" onClick={() => setIsEditing(false)}>
            취소
          </button>
          {updateMutation.isError && (
            <span className="collection-inline-error">{getApiErrorMessage(updateMutation.error)}</span>
          )}
        </td>
      </tr>
    )
  }

  return (
    <tr>
      <td>{target.dataNameKo}</td>
      <td>{target.enabled ? '활성' : '비활성'}</td>
      <td>
        {target.scheduleType} / {target.intervalValue}
      </td>
      <td>{formatDateTime(target.lastExecutedAt)}</td>
      <td>{formatDateTime(target.nextExecutedAt)}</td>
      <td>
        <span
          className={`collection-status collection-status-${(target.latestStatus ?? 'none').toLowerCase()}`}
        >
          {target.latestStatus ?? '-'}
        </span>
      </td>
      <td className="collection-row-actions">
        <button
          type="button"
          onClick={() => executeMutation.mutate(target.id)}
          disabled={executeMutation.isPending}
        >
          {executeMutation.isPending ? '실행 중...' : '수동 실행'}
        </button>
        <button type="button" onClick={startEditing}>
          설정
        </button>
        <button type="button" onClick={() => onSelectTarget(target.id)}>
          이력 보기
        </button>
        {executeMutation.isError && (
          <span className="collection-inline-error">{getApiErrorMessage(executeMutation.error)}</span>
        )}
      </td>
    </tr>
  )
}
