<template>
  <common-monitor
    config-key="apiTestMonitor"
    monitor-name="API测试监控"
    css-prefix="api-test"
    :desc-column="4"
  />
</template>

<script>
import CommonMonitor from '../CommonMonitor.vue'

export default {
  name: 'ApiTestMonitor',
  components: {
    CommonMonitor
  }
};
</script>

