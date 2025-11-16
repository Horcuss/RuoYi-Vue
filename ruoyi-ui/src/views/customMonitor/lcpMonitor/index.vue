<template>
  <common-monitor
    config-key="lcpTestComplete"
    monitor-name="LCP监控"
    css-prefix="lcp"
    :desc-column="4"
  />
</template>

<script>
import CommonMonitor from '../CommonMonitor.vue'

export default {
  name: 'lcpTestComplete',
  components: {
    CommonMonitor
  }
};
</script>
