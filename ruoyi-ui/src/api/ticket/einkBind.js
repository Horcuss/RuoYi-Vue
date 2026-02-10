import request from '@/utils/request'

// 绑定水墨屏与lot
export function bindEink(data) {
  return request({
    url: '/api/ticket/eink/bind',
    method: 'post',
    data: data
  })
}
