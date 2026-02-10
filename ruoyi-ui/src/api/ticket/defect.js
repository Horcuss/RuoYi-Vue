import request from '@/utils/request'

// 获取lot当前工序
export function getCurrentProcess(lotNo) {
  return request({
    url: '/api/ticket/defect/current-process/' + lotNo,
    method: 'get'
  })
}

// 提交不良数据
export function submitDefect(data) {
  return request({
    url: '/api/ticket/defect/submit',
    method: 'post',
    data: data
  })
}

// 查询lot不良明细列表
export function getDefectList(lotNo) {
  return request({
    url: '/api/ticket/defect/list/' + lotNo,
    method: 'get'
  })
}
