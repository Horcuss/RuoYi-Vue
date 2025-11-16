<template>
  <common-monitor
    config-key="gpTestComplete"
    monitor-name="GP监控"
    css-prefix="gp"
    :desc-column="4"
  />
</template>

<script>
import CommonMonitor from '../CommonMonitor.vue'

export default {
  name: 'gpTestComplete',
  components: {
    CommonMonitor
  }
};
</script>