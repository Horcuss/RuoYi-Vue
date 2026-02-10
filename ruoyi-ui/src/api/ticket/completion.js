import request from '@/utils/request'

// 提交完了输机数据
export function submitCompletion(data) {
  return request({
    url: '/api/ticket/completion/submit',
    method: 'post',
    data: data
  })
}
