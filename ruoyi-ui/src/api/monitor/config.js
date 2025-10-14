import request from '@/utils/request'

// 查询监控配置列表
export function listConfig(query) {
  return request({
    url: '/monitor/config/list',
    method: 'get',
    params: query
  })
}

// 查询监控配置详细
export function getConfig(configId) {
  return request({
    url: '/monitor/config/' + configId,
    method: 'get'
  })
}

// 根据配置KEY查询监控配置
export function getConfigByKey(configKey) {
  return request({
    url: '/monitor/config/key/' + configKey,
    method: 'get'
  })
}

// 新增监控配置
export function addConfig(data) {
  return request({
    url: '/monitor/config',
    method: 'post',
    data: data
  })
}

// 修改监控配置
export function updateConfig(data) {
  return request({
    url: '/monitor/config',
    method: 'put',
    data: data
  })
}

// 删除监控配置
export function delConfig(configId) {
  return request({
    url: '/monitor/config/' + configId,
    method: 'delete'
  })
}

