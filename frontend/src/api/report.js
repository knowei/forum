import request from '@/utils/request'

export function reportResource(resourceId, reason) {
  return request.post(`/resource/${resourceId}/report`, { reason })
}

export function getReportList() {
  return request.get('/admin/report/list')
}

export function handleReport(id, data) {
  return request.put(`/admin/report/${id}/status`, data)
}
