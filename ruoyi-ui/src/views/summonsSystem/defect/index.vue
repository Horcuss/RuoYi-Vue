<template>
  <div class="app-container">
    <el-card shadow="hover">
      <div slot="header">
        <span>不良入力</span>
      </div>

      <el-form ref="form" :model="form" :rules="rules" label-width="120px" style="max-width: 600px;">
        <el-form-item label="lot" prop="lotNo">
          <el-input v-model="form.lotNo" placeholder="请输入lot编号" clearable @blur="handleLotBlur" />
        </el-form-item>

        <el-form-item label="工程">
          <el-input v-model="form.processName" readonly placeholder="输入lot后自动带出" />
        </el-form-item>

        <el-form-item label="不良项目" prop="defectItem">
          <el-select v-model="form.defectItem" placeholder="请选择不良项目">
            <el-option label="针孔" value="针孔" />
            <el-option label="划伤" value="划伤" />
            <el-option label="气泡" value="气泡" />
            <el-option label="变色" value="变色" />
            <el-option label="异物" value="异物" />
            <el-option label="尺寸不良" value="尺寸不良" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <el-form-item label="取样数量" prop="sampleCount">
          <el-input-number v-model="form.sampleCount" :min="1" style="width: 100%;" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">提 交</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 不良明细表格 -->
    <el-card shadow="hover" style="margin-top: 20px;">
      <div slot="header">
        <span>不良明细</span>
      </div>

      <el-table :data="defectList" border style="width: 100%;" v-loading="tableLoading">
        <el-table-column prop="processName" label="工程" width="150" />
        <el-table-column prop="defectItem" label="不良项目" width="150" />
        <el-table-column prop="sampleCount" label="不良数" width="120" />
        <el-table-column label="合计" width="120">
          <template slot-scope="scope">
            <span v-if="scope.$index === defectList.length - 1 || isLastOfGroup(scope.$index)">
              {{ getGroupTotal(scope.row.processName) }}
            </span>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="totalCount > 0" style="margin-top: 10px; text-align: right; font-weight: bold;">
        总合计: {{ totalCount }}
      </div>
    </el-card>
  </div>
</template>

<script>
import { getCurrentProcess, submitDefect, getDefectList } from '@/api/ticket/defect'

export default {
  name: 'Defect',
  data() {
    return {
      form: {
        lotNo: '',
        processCode: '',
        processName: '',
        defectItem: '',
        sampleCount: 1
      },
      rules: {
        lotNo: [{ required: true, message: '请输入lot编号', trigger: 'blur' }],
        defectItem: [{ required: true, message: '请选择不良项目', trigger: 'change' }],
        sampleCount: [{ required: true, message: '请输入取样数量', trigger: 'blur' }]
      },
      submitting: false,
      tableLoading: false,
      defectList: [],
      totalCount: 0
    }
  },
  methods: {
    handleLotBlur() {
      if (!this.form.lotNo) return

      // 查询当前工序
      getCurrentProcess(this.form.lotNo).then(res => {
        if (res.code === 200 && res.data) {
          this.form.processCode = res.data.processCode
          this.form.processName = res.data.processName
        } else {
          this.form.processCode = ''
          this.form.processName = ''
          if (res.msg) {
            this.$message.warning(res.msg)
          }
        }
      }).catch(() => {
        this.form.processCode = ''
        this.form.processName = ''
      })

      // 加载不良明细
      this.loadDefectList()
    },

    loadDefectList() {
      if (!this.form.lotNo) return

      this.tableLoading = true
      getDefectList(this.form.lotNo).then(res => {
        if (res.code === 200 && res.data) {
          this.defectList = res.data.details || []
          this.totalCount = res.data.total || 0
        } else {
          this.defectList = []
          this.totalCount = 0
        }
      }).catch(() => {
        this.defectList = []
        this.totalCount = 0
      }).finally(() => {
        this.tableLoading = false
      })
    },

    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (!valid) return

        if (!this.form.processCode) {
          this.$message.warning('未获取到工序信息，请先输入正确的lot编号')
          return
        }

        this.submitting = true

        submitDefect(this.form).then(res => {
          if (res.code === 200) {
            this.$message.success('提交成功')
            // 清空输入但保留lot
            this.form.defectItem = ''
            this.form.sampleCount = 1
            // 刷新表格
            this.loadDefectList()
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

    /** 判断是否是当前工程分组的最后一条 */
    isLastOfGroup(index) {
      if (index >= this.defectList.length - 1) return true
      return this.defectList[index].processName !== this.defectList[index + 1].processName
    },

    /** 计算某工程的合计 */
    getGroupTotal(processName) {
      let total = 0
      this.defectList.forEach(item => {
        if (item.processName === processName) {
          total += (item.sampleCount || 0)
        }
      })
      return total
    }
  }
}
</script>
