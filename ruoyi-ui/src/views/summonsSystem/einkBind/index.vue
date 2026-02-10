<template>
  <div class="app-container">
    <el-card shadow="hover">
      <div slot="header">
        <span>水墨屏绑定</span>
      </div>

      <el-form ref="bindForm" :model="form" :rules="rules" label-width="120px" style="max-width: 600px;">
        <el-form-item label="水墨屏编号" prop="rfid">
          <el-input v-model="form.rfid" placeholder="等待感应器读取..." readonly>
            <template slot="append">
              <el-tag v-if="wsConnected" type="success" size="small">已连接</el-tag>
              <el-tag v-else type="danger" size="small">未连接</el-tag>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="当前工序">
          <el-input v-model="form.processName" placeholder="等待感应器读取..." readonly />
        </el-form-item>

        <el-form-item label="lot" prop="lotNo">
          <el-input v-model="form.lotNo" placeholder="请输入lot编号" clearable />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">提 交</el-button>
        </el-form-item>
      </el-form>

      <div v-if="bindResult" style="margin-top: 20px;">
        <el-alert
          :title="bindResult.success ? '绑定成功' : '绑定失败'"
          :type="bindResult.success ? 'success' : 'error'"
          :description="bindResult.message"
          show-icon
          :closable="false"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { bindEink } from '@/api/ticket/einkBind'

export default {
  name: 'EinkBind',
  data() {
    return {
      form: {
        rfid: '',
        lotNo: '',
        processCode: '',
        processName: ''
      },
      rules: {
        rfid: [{ required: true, message: '请等待感应器读取水墨屏编号', trigger: 'blur' }],
        lotNo: [{ required: true, message: '请输入lot编号', trigger: 'blur' }]
      },
      wsConnected: false,
      ws: null,
      submitting: false,
      bindResult: null
    }
  },
  mounted() {
    this.connectWebSocket()
  },
  beforeDestroy() {
    if (this.ws) {
      this.ws.close()
    }
  },
  methods: {
    connectWebSocket() {
      const wsUrl = `ws://${window.location.host}/ws/sensor`
      try {
        this.ws = new WebSocket(wsUrl)

        this.ws.onopen = () => {
          this.wsConnected = true
          console.log('WebSocket 已连接')
        }

        this.ws.onmessage = (event) => {
          try {
            const data = JSON.parse(event.data)
            if (data.type === 'bind') {
              this.form.rfid = data.rfid
              this.form.processCode = data.processCode
              this.form.processName = data.processName
            }
          } catch (e) {
            console.error('解析WS消息失败', e)
          }
        }

        this.ws.onclose = () => {
          this.wsConnected = false
          console.log('WebSocket 已断开，5秒后重连...')
          setTimeout(() => this.connectWebSocket(), 5000)
        }

        this.ws.onerror = (error) => {
          this.wsConnected = false
          console.error('WebSocket 错误', error)
        }
      } catch (e) {
        console.error('WebSocket 连接失败', e)
      }
    },

    handleSubmit() {
      this.$refs.bindForm.validate(valid => {
        if (!valid) return

        if (!this.form.processCode) {
          this.$message.warning('请等待感应器读取工序信息')
          return
        }

        this.submitting = true
        this.bindResult = null

        bindEink({
          rfid: this.form.rfid,
          lotNo: this.form.lotNo,
          processCode: this.form.processCode
        }).then(res => {
          if (res.code === 200) {
            this.bindResult = {
              success: true,
              message: `绑定成功 - lot: ${res.data.lotNo}, 工序: ${res.data.processName}, 品名: ${res.data.productName}, 工序总数: ${res.data.sequenceCount}`
            }
            this.$message.success('绑定成功')
          } else {
            this.bindResult = { success: false, message: res.msg }
            this.$message.error(res.msg)
          }
        }).catch(err => {
          this.bindResult = { success: false, message: err.message || '绑定失败' }
          this.$message.error('绑定失败')
        }).finally(() => {
          this.submitting = false
        })
      })
    }
  }
}
</script>
