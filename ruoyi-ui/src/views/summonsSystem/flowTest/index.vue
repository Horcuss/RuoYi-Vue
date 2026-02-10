<template>
  <div class="app-container">
    <!-- 顶部：lot输入 + 加载 -->
    <el-card shadow="hover">
      <div slot="header"><span>工序流转测试工具</span></div>
      <el-row :gutter="10" style="margin-bottom: 12px;">
        <el-col :span="6">
          <el-input v-model="lotNo" placeholder="如 LOT20260210001" clearable @keyup.enter.native="loadData">
            <template slot="prepend">lot</template>
          </el-input>
        </el-col>
        <el-col :span="18">
          <el-button type="primary" @click="loadData" :loading="loading">加载工序 & 状态</el-button>
          <el-button type="warning" @click="handleReset" :disabled="!hasData">重置到第一道工序</el-button>
          <el-button type="danger" @click="clearLog">清空日志</el-button>
        </el-col>
      </el-row>

      <!-- 快速初始化（未绑定时显示） -->
      <el-alert v-if="showInitTip" type="warning" :closable="false" style="margin-bottom: 12px;">
        <template slot="title">
          该lot还没绑定过水墨屏，数据库里没有工序序列和流转状态。点下面的按钮一键初始化。
        </template>
      </el-alert>
      <el-row :gutter="10" v-if="showInitTip || !hasData">
        <el-col :span="4">
          <el-select v-model="initProcessCode" placeholder="起始工序" style="width: 100%;">
            <el-option label="2350 dh处理" value="2350" />
            <el-option label="2400 电镀" value="2400" />
            <el-option label="2500 电镀后热处理" value="2500" />
            <el-option label="3300 一回测定" value="3300" />
            <el-option label="3410 后始末" value="3410" />
            <el-option label="3500 真空热处理" value="3500" />
            <el-option label="3550 mips" value="3550" />
            <el-option label="3600 g2外选" value="3600" />
            <el-option label="4100 gp" value="4100" />
            <el-option label="4400 磁石吸取" value="4400" />
            <el-option label="4500 清空" value="4500" />
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-button type="success" :loading="initing" @click="handleInit">
            一键初始化（绑定 + 生成工序 + 初始化状态）
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 流转状态 -->
    <el-card shadow="hover" style="margin-top: 12px;" v-if="flowStatus">
      <div slot="header"><span>当前流转状态</span></div>
      <el-descriptions :column="4" border size="medium">
        <el-descriptions-item label="当前工序">
          <el-tag :type="flowStatus.status === 'ABNORMAL' ? 'danger' : 'primary'" size="medium">
            {{ flowStatus.currentProcessCode }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="序号">{{ flowStatus.currentSeq }}</el-descriptions-item>
        <el-descriptions-item label="已离开">
          <el-tag :type="flowStatus.hasExited === 1 ? 'warning' : 'info'" size="small">
            {{ flowStatus.hasExited === 1 ? '是 (已OUT)' : '否 (还在)' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="flowStatus.status === 'NORMAL' ? 'success' : 'danger'" size="medium" effect="dark">
            {{ flowStatus.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="测定回数">{{ flowStatus.testingRound || 0 }}</el-descriptions-item>
        <el-descriptions-item label="测定工序">{{ flowStatus.testingProcessCode || '未设定' }}</el-descriptions-item>
        <el-descriptions-item label="异常信息" :span="2">
          <span v-if="flowStatus.abnormalMsg" style="color: #F56C6C; font-weight: bold;">{{ flowStatus.abnormalMsg }}</span>
          <span v-else style="color: #909399;">无</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 工序流水线 -->
    <el-card shadow="hover" style="margin-top: 12px;" v-if="sequences.length > 0">
      <div slot="header"><span>工序序列（点按钮模拟感应器触发）</span></div>
      <div class="process-pipeline">
        <div
          v-for="(proc, index) in sequences"
          :key="proc.processCode"
          class="process-node"
          :class="getNodeClass(proc)"
        >
          <div class="process-header">
            <span class="process-seq">#{{ proc.seq }}</span>
            <span class="process-name">{{ proc.processName }}</span>
            <span class="process-code">{{ proc.processCode }}</span>
          </div>
          <div class="process-actions">
            <el-button
              size="mini"
              type="success"
              :disabled="simulating"
              @click="simulate(proc.processCode, 'IN')"
            >IN</el-button>
            <el-button
              size="mini"
              type="danger"
              :disabled="simulating"
              @click="simulate(proc.processCode, 'OUT')"
            >OUT</el-button>
          </div>
          <div class="process-arrow" v-if="index < sequences.length - 1">→</div>
        </div>
      </div>

      <!-- 测定棚模拟区 -->
      <div style="margin-top: 16px; padding: 12px; background: #FDF6EC; border-radius: 4px;">
        <span style="font-weight: bold; color: #E6A23C;">测定棚模拟（isTestingEvent=true）：</span>
        <div style="margin-top: 8px; display: flex; align-items: center; gap: 8px;">
          <el-select v-model="testingCode" placeholder="选择测定工序" size="small" style="width: 200px;">
            <el-option
              v-for="proc in sequences"
              :key="proc.processCode"
              :label="proc.processCode + ' ' + proc.processName"
              :value="proc.processCode"
            />
          </el-select>
          <el-button size="small" type="success" :disabled="simulating || !testingCode" @click="simulateTesting('IN')">
            测定棚 IN
          </el-button>
          <el-button size="small" type="danger" :disabled="simulating || !testingCode" @click="simulateTesting('OUT')">
            测定棚 OUT
          </el-button>
          <span style="color: #909399; font-size: 12px;">
            （选好测定工序后，IN/OUT事件会带 isTestingEvent=true 标志）
          </span>
        </div>
      </div>
    </el-card>

    <!-- 事件日志 -->
    <el-card shadow="hover" style="margin-top: 12px;">
      <div slot="header">
        <span>事件日志</span>
        <span style="float: right; color: #909399; font-size: 12px;">共 {{ eventLog.length }} 条</span>
      </div>
      <div class="event-log" ref="logContainer">
        <div v-if="eventLog.length === 0" style="color: #C0C4CC; text-align: center; padding: 20px;">
          暂无事件，请先加载lot后点击 IN/OUT 按钮
        </div>
        <div
          v-for="(log, index) in eventLog"
          :key="index"
          class="log-item"
          :class="getLogClass(log)"
        >
          <span class="log-time">{{ log.time }}</span>
          <el-tag :type="log.eventType === 'IN' ? 'success' : 'warning'" size="mini" style="margin: 0 6px;">
            {{ log.eventType }}
          </el-tag>
          <span class="log-process">{{ log.processCode }}</span>
          <el-tag v-if="log.isTesting" type="warning" size="mini" effect="plain" style="margin-left: 4px;">测定棚</el-tag>
          <span class="log-arrow">→</span>
          <span class="log-result" :class="{ 'log-abnormal': log.abnormal, 'log-transition': log.transitioned }">
            {{ log.message }}
          </span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { simulateEvent, getFlowStatus, getFlowSequence, resetFlowStatus } from '@/api/ticket/flowTest'
import { bindEink } from '@/api/ticket/einkBind'

export default {
  name: 'FlowTest',
  data() {
    return {
      lotNo: 'LOT20260210001',
      initProcessCode: '2350',
      testingCode: '',
      loading: false,
      initing: false,
      simulating: false,
      showInitTip: false,
      flowStatus: null,
      sequences: [],
      eventLog: []
    }
  },
  computed: {
    hasData() {
      return this.flowStatus !== null && this.sequences.length > 0
    }
  },
  methods: {
    loadData() {
      if (!this.lotNo) {
        this.$message.warning('请输入lot编号')
        return
      }
      this.loading = true
      Promise.all([
        getFlowStatus(this.lotNo),
        getFlowSequence(this.lotNo)
      ]).then(([statusRes, seqRes]) => {
        if (statusRes.code === 200) {
          this.flowStatus = statusRes.data
          // 自动填充测定工序
          if (this.flowStatus.testingProcessCode) {
            this.testingCode = this.flowStatus.testingProcessCode
          }
        } else {
          this.$message.error(statusRes.msg)
          this.flowStatus = null
        }
        if (seqRes.code === 200) {
          this.sequences = seqRes.data
        } else {
          this.$message.error(seqRes.msg)
          this.sequences = []
        }
      }).catch(() => {
        this.$message.error('加载失败')
      }).finally(() => {
        this.loading = false
        if (!this.flowStatus && this.sequences.length === 0) {
          this.showInitTip = true
        } else {
          this.showInitTip = false
        }
      })
    },

    handleInit() {
      if (!this.lotNo) {
        this.$message.warning('请输入lot编号')
        return
      }
      this.initing = true
      const rfid = 'RFID_TEST_' + Date.now()
      bindEink({
        rfid: rfid,
        lotNo: this.lotNo,
        processCode: this.initProcessCode
      }).then(res => {
        if (res.code === 200) {
          this.$message.success('初始化成功！rfid=' + rfid + '，工序数=' + res.data.sequenceCount)
          this.showInitTip = false
          this.loadData()
        } else {
          this.$message.error('初始化失败: ' + res.msg)
        }
      }).catch(() => {
        this.$message.error('初始化请求失败')
      }).finally(() => {
        this.initing = false
      })
    },

    simulate(processCode, eventType, isTestingEvent = false) {
      if (!this.lotNo) return
      this.simulating = true

      simulateEvent({
        lotNo: this.lotNo,
        processCode: processCode,
        eventType: eventType,
        isTestingEvent: isTestingEvent
      }).then(res => {
        const timeStr = this.nowStr()

        if (res.code === 200) {
          const data = res.data

          this.eventLog.push({
            time: timeStr,
            processCode: processCode,
            eventType: eventType,
            isTesting: isTestingEvent,
            message: data.message,
            transitioned: data.transitioned,
            abnormal: data.abnormal
          })

          if (data.currentStatus) {
            this.flowStatus = data.currentStatus
            // 自动更新测定工序选择
            if (this.flowStatus.testingProcessCode && !this.testingCode) {
              this.testingCode = this.flowStatus.testingProcessCode
            }
          }

          this.$nextTick(() => {
            const container = this.$refs.logContainer
            if (container) {
              container.scrollTop = container.scrollHeight
            }
          })
        } else {
          this.$message.error(res.msg)
        }
      }).catch(() => {
        this.$message.error('模拟事件调用失败')
      }).finally(() => {
        this.simulating = false
      })
    },

    simulateTesting(eventType) {
      if (!this.testingCode) {
        this.$message.warning('请先选择测定工序')
        return
      }
      this.simulate(this.testingCode, eventType, true)
    },

    handleReset() {
      this.$confirm('确认将流转状态重置到第一道工序？（测定回数和测定工序标记也会清零）', '提示', {
        type: 'warning'
      }).then(() => {
        resetFlowStatus(this.lotNo, {}).then(res => {
          if (res.code === 200) {
            this.flowStatus = res.data
            this.testingCode = ''
            this.eventLog.push({
              time: this.nowStr(),
              processCode: '-',
              eventType: 'RESET',
              isTesting: false,
              message: '>>> 已重置到第一道工序 <<<',
              transitioned: false,
              abnormal: false
            })
            this.$message.success('已重置')
          } else {
            this.$message.error(res.msg)
          }
        })
      }).catch(() => {})
    },

    clearLog() {
      this.eventLog = []
    },

    nowStr() {
      const now = new Date()
      return now.getHours().toString().padStart(2, '0') + ':' +
             now.getMinutes().toString().padStart(2, '0') + ':' +
             now.getSeconds().toString().padStart(2, '0')
    },

    getNodeClass(proc) {
      if (!this.flowStatus) return ''
      const current = this.flowStatus.currentProcessCode
      if (proc.processCode === current) {
        if (this.flowStatus.status === 'ABNORMAL') return 'node-abnormal'
        if (this.flowStatus.hasExited === 1) return 'node-exited'
        return 'node-current'
      }
      if (proc.seq < this.flowStatus.currentSeq) return 'node-done'
      return ''
    },

    getLogClass(log) {
      if (log.abnormal) return 'log-item-abnormal'
      if (log.transitioned) return 'log-item-transition'
      return ''
    }
  }
}
</script>

<style scoped>
.process-pipeline {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: flex-start;
}
.process-node {
  position: relative;
  border: 2px solid #DCDFE6;
  border-radius: 6px;
  padding: 8px 12px;
  min-width: 120px;
  text-align: center;
  background: #FAFAFA;
  transition: all 0.3s;
}
.process-node .process-header {
  margin-bottom: 6px;
}
.process-node .process-seq {
  font-size: 11px;
  color: #909399;
  margin-right: 4px;
}
.process-node .process-name {
  font-weight: bold;
  font-size: 13px;
  display: block;
}
.process-node .process-code {
  font-size: 11px;
  color: #909399;
}
.process-node .process-actions {
  display: flex;
  justify-content: center;
  gap: 4px;
}
.process-node .process-arrow {
  position: absolute;
  right: -16px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 16px;
  color: #C0C4CC;
}

/* 状态样式 */
.node-current {
  border-color: #409EFF;
  background: #ECF5FF;
  box-shadow: 0 0 8px rgba(64, 158, 255, 0.4);
}
.node-exited {
  border-color: #E6A23C;
  background: #FDF6EC;
  box-shadow: 0 0 8px rgba(230, 162, 60, 0.4);
}
.node-abnormal {
  border-color: #F56C6C;
  background: #FEF0F0;
  box-shadow: 0 0 8px rgba(245, 108, 108, 0.5);
  animation: pulse-red 1s infinite;
}
.node-done {
  border-color: #67C23A;
  background: #F0F9EB;
}

@keyframes pulse-red {
  0%, 100% { box-shadow: 0 0 8px rgba(245, 108, 108, 0.5); }
  50% { box-shadow: 0 0 16px rgba(245, 108, 108, 0.8); }
}

/* 日志样式 */
.event-log {
  max-height: 350px;
  overflow-y: auto;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
}
.log-item {
  padding: 4px 8px;
  border-bottom: 1px solid #F2F6FC;
  line-height: 28px;
}
.log-item:hover {
  background: #F5F7FA;
}
.log-time {
  color: #909399;
  margin-right: 6px;
}
.log-process {
  font-weight: bold;
  color: #303133;
}
.log-arrow {
  margin: 0 6px;
  color: #C0C4CC;
}
.log-result {
  color: #606266;
}
.log-abnormal {
  color: #F56C6C !important;
  font-weight: bold;
}
.log-transition {
  color: #67C23A !important;
  font-weight: bold;
}
.log-item-abnormal {
  background: #FEF0F0;
}
.log-item-transition {
  background: #F0F9EB;
}
</style>
