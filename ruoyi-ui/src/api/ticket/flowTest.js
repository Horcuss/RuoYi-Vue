import request from '@/utils/request'

// 模拟感应器事件
export function simulateEvent(data) {
  return request({
    url: '/api/ticket/flow/simulate',
    method: 'post',
    data: data
  })
}

// 查询流转状态
export function getFlowStatus(lotNo) {
  return request({
    url: '/api/ticket/flow/status/' + lotNo,
    method: 'get'
  })
}

// 查询工序序列
export function getFlowSequence(lotNo) {
  return request({
    url: '/api/ticket/flow/sequence/' + lotNo,
    method: 'get'
  })
}

// 重置流转状态
export function resetFlowStatus(lotNo, data) {
  return request({
    url: '/api/ticket/flow/reset/' + lotNo,
    method: 'post',
    data: data
  })
}
