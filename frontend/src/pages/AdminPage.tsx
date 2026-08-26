import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQueryClient } from '@tanstack/react-query'
import { useAuthStore } from '../features/auth/store/authStore'
import { CollectionSummaryCards } from '../features/collection/components/CollectionSummaryCards'
import { CollectionTargetTable } from '../features/collection/components/CollectionTargetTable'
import { CollectionJobTable } from '../features/collection/components/CollectionJobTable'
import { CollectionJobDetailPanel } from '../features/collection/components/CollectionJobDetailPanel'

export function AdminPage() {
  const [jobFilterTargetId, setJobFilterTargetId] = useState<number | null>(null)
  const [selectedJobId, setSelectedJobId] = useState<number | null>(null)

  const user = useAuthStore((state) => state.user)
  const clearAuth = useAuthStore((state) => state.clearAuth)
  const queryClient = useQueryClient()
  const navigate = useNavigate()

  function handleLogout() {
    clearAuth()
    queryClient.clear()
    navigate('/login', { replace: true })
  }

  return (
    <section className="collection-dashboard">
      <div className="collection-dashboard-header">
        <h1>수집 대시보드</h1>
        <div className="collection-dashboard-user">
          <span>
            {user?.loginId}님 (권한: {user?.role})
          </span>
          <button type="button" onClick={handleLogout}>
            로그아웃
          </button>
        </div>
      </div>

      <CollectionSummaryCards />

      <div className="collection-dashboard-body">
        <div className="collection-dashboard-main">
          <h2>수집 대상</h2>
          <CollectionTargetTable onSelectTarget={setJobFilterTargetId} />

          <h2>수집 이력</h2>
          <CollectionJobTable
            key={jobFilterTargetId ?? 'all'}
            initialTargetId={jobFilterTargetId}
            selectedJobId={selectedJobId}
            onSelectJob={setSelectedJobId}
          />
        </div>

        <CollectionJobDetailPanel jobId={selectedJobId} onClose={() => setSelectedJobId(null)} />
      </div>
    </section>
  )
}
