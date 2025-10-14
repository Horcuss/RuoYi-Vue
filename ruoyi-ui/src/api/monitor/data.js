import request from '@/utils/request'

// 根据配置KEY和参数获取监控数据
export function getMonitorDataWithParams(configKey, params) {
  return request({
    url: '/monitor/data/' + configKey,
    method: 'post',
    data: params
  })
}

