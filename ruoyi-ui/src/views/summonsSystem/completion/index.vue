<template>
  <div class="app-container">
    <el-card shadow="hover">
      <div slot="header">
        <span>完了输机</span>
      </div>

      <el-form ref="form" :model="form" :rules="rules" label-width="120px" style="max-width: 600px;">
        <el-form-item label="工程" prop="processCode">
          <el-select v-model="form.processCode" placeholder="请选择工程" @change="handleProcessChange">
            <el-option label="gp" value="4100" />
            <el-option label="磁石吸取" value="4400" />
            <el-option label="清空" value="4500" />
          </el-select>
        </el-form-item>

        <el-form-item label="lot" prop="lotNo">
          <el-input v-model="form.lotNo" placeholder="请输入lot编号" clearable />
        </el-form-item>

        <el-form-item label="作业日" prop="workDate">
          <el-date-picker
            v-model="form.workDate"
            type="date"
            placeholder="选择日期"
            value-format="yyyy-MM-dd"
            style="width: 100%;"
          />
        </el-form-item>

        <el-form-item label="作业者" prop="worker">
          <el-input v-model="form.worker" placeholder="请输入作业者" clearable />
        </el-form-item>

        <el-form-item label="完了数" prop="completionCount">
          <el-input-number v-model="form.completionCount" :min="0" style="width: 100%;" />
        </el-form-item>

        <!-- gp 特有字段 -->
        <template v-if="form.processCode === '4100'">
          <el-form-item label="薄膜卷号">
            <el-input v-model="form.filmRollNo" placeholder="请输入薄膜卷号" clearable />
          </el-form-item>
          <el-form-item label="不良信息">
            <el-input v-model="form.defectInfo" type="textarea" :rows="3" placeholder="请输入不良信息" />
          </el-form-item>
        </template>

        <!-- 磁石吸取 特有字段 -->
        <template v-if="form.processCode === '4400'">
          <el-form-item label="设备号">
            <el-input v-model="form.deviceNo" placeholder="请输入设备号" clearable />
          </el-form-item>
        </template>

        <!-- 清空 特有字段 -->
        <template v-if="form.processCode === '4500'">
          <el-form-item label="枚数">
            <el-input-number v-model="form.quantity" :min="0" style="width: 100%;" />
          </el-form-item>
        </template>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">提 交</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { submitCompletion } from '@/api/ticket/completion'

// 工程code到中文名的映射
const processNameMap = {
  '4100': 'gp',
  '4400': '磁石吸取',
  '4500': '清空'
}

export default {
  name: 'Completion',
  data() {
    return {
      form: {
        processCode: '',
        processName: '',
        lotNo: '',
        workDate: '',
        worker: '',
        completionCount: null,
        filmRollNo: '',
        defectInfo: '',
        deviceNo: '',
        quantity: null
      },
      rules: {
        processCode: [{ required: true, message: '请选择工程', trigger: 'change' }],
        lotNo: [{ required: true, message: '请输入lot编号', trigger: 'blur' }],
        workDate: [{ required: true, message: '请选择作业日', trigger: 'change' }],
        worker: [{ required: true, message: '请输入作业者', trigger: 'blur' }],
        completionCount: [{ required: true, message: '请输入完了数', trigger: 'blur' }]
      },
      submitting: false
    }
  },
  methods: {
    handleProcessChange(val) {
      this.form.processName = processNameMap[val] || ''
      // 切换工程时清空特有字段
      this.form.filmRollNo = ''
      this.form.defectInfo = ''
      this.form.deviceNo = ''
      this.form.quantity = null
    },

    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (!valid) return

        this.submitting = true

        submitCompletion(this.form).then(res => {
          if (res.code === 200) {
            this.$message.success('提交成功')
            this.resetForm()
          } else {
            this.$message.error(res.msg)
          }
        }).catch(() => {
          this.$message.error('提交失败')
        }).finally(() => {
          this.submitting = false
        })
      })
    },

    resetForm() {
      // 保留工程选择，清空其他字段
      const processCode = this.form.processCode
      const processName = this.form.processName
      this.$refs.form.resetFields()
      this.form.processCode = processCode
      this.form.processName = processName
    }
  }
}
</script>
