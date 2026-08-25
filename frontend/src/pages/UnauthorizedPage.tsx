import { Link } from 'react-router-dom'

export function UnauthorizedPage() {
  return (
    <section>
      <h1>접근 권한이 없습니다</h1>
      <p>이 페이지에 접근할 수 있는 권한이 없습니다.</p>
      <Link to="/">홈으로 이동</Link>
    </section>
  )
}
