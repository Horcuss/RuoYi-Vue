<template>
  <div class="instruction-table-wrapper">
    <el-card class="instruction-card" shadow="never">
      <div slot="header" class="instruction-header">
        <i class="el-icon-document"></i>
        <span>指示书信息</span>
      </div>
      
      <!-- 使用原生table实现复杂表头 -->
      <table class="instruction-table">
        <thead>
          <tr>
            <th rowspan="2">发行区分</th>
            <th rowspan="2">指示书NO</th>
            <th>改订记录</th>
            <th>文件类别名</th>
            <th colspan="2">指示书名</th>
            <th>本文</th>
            <th>加工条件</th>
            <th>追加情报</th>
          </tr>
          <tr>
            <th colspan="2">lot no／品番・工程系列</th>
            <th colspan="1">中分类名</th>
            <th colspan="4">comment</th>
          </tr>
        </thead>
        <tbody v-for="(row, index) in tableData" :key="index">
          <!-- 第一行：主要信息 -->
          <tr>
            <td rowspan="2">{{ row.issueType || '' }}</td>
            <td rowspan="2">{{ row.instructionNo || '' }}</td>
            <td>{{ row.revisionRecord || '' }}</td>
            <td>{{ row.documentType || '' }}</td>
            <td colspan="2">{{ row.instructionName || '' }}</td>
            <td>
              <el-button
                v-if="row.mainTextTitle && row.mainTextBlobId"
                type="primary"
                size="mini"
                @click="handleMainTextClick(row)"
              >
                {{ row.mainTextTitle }}
              </el-button>
            </td>
            <td>
              <el-button
                v-if="row.processingConditionTitle && row.processingConditionBlobId"
                type="primary"
                size="mini"
                @click="handleProcessingConditionClick(row)"
              >
                {{ row.processingConditionTitle }}
              </el-button>
            </td>
            <td>
              <el-button
                v-if="row.additionalInfoTitle && row.additionalInfoBlobId"
                type="primary"
                size="mini"
                @click="handleAdditionalInfoClick(row)"
              >
                {{ row.additionalInfoTitle }}
              </el-button>
            </td>
          </tr>
          <!-- 第二行：lot信息 -->
          <tr>
            <td colspan="2">{{ row.lotInfo || '' }}</td>
            <td colspan="1">{{ row.middleClassName || '' }}</td>
            <td colspan="4">{{ row.comment || '' }}</td>
          </tr>
        </tbody>
      </table>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'InstructionTable',
  props: {
    // 表格数据
    tableData: {
      type: Array,
      default: () => []
    },
    // 项目类型（用于判断显示lotno还是品番・工程系列）
    isGpProject: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    /**
     * 本文按钮点击（暂不实现FTP功能）
     */
    handleMainTextClick(row) {
      console.log('点击本文按钮:', row);
      // TODO: 未来通过FTP打开文件，使用 row.mainTextBlobId
      this.$message.info('本文功能暂未实现');
    },

    /**
     * 加工条件按钮点击（新开标签页）
     */
    handleProcessingConditionClick(row) {
      console.log('点击加工条件按钮:', row);
      if (row.instructionNo) {
        // TODO: 将API地址配置化
        const apiUrl = 'http://example.com/processing-condition';
        const fullUrl = `${apiUrl}?instructionNo=${encodeURIComponent(row.instructionNo)}`;
        window.open(fullUrl, '_blank');
      } else {
        this.$message.warning('指示书NO为空，无法打开加工条件');
      }
    },

    /**
     * 追加情报按钮点击（暂不实现FTP功能）
     */
    handleAdditionalInfoClick(row) {
      console.log('点击追加情报按钮:', row);
      // TODO: 未来通过FTP打开文件，使用 row.additionalInfoBlobId
      this.$message.info('追加情报功能暂未实现');
    }
  }
};
</script>

<style scoped>
.instruction-table-wrapper {
  margin-top: 20px;
}

.instruction-card {
  background-color: #fafbfc;
  border: 1px solid #e6ebf5;
}

.instruction-card >>> .el-card__header {
  padding: 12px 20px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e6ebf5;
}

.instruction-header {
  display: flex;
  align-items: center;
  font-weight: 500;
  color: #333b5b;
  font-size: 14px;
}

.instruction-header i {
  margin-right: 8px;
  font-size: 16px;
  color: #409eff;
}

/* 原生表格样式 */
.instruction-table {
  border-collapse: collapse;
  width: 100%;
  background: #fff;
}

.instruction-table th,
.instruction-table td {
  border: 1px solid #111;
  text-align: center;
  padding: 8px 6px;
  font-size: 14px;
  vertical-align: middle;
}

.instruction-table thead tr:first-child th {
  background: #f0f0f0;
  font-weight: 600;
  color: #333;
}

.instruction-table thead tr:nth-child(2) th {
  background: #f0f0f0;
  font-weight: 600;
  color: #333;
}

.instruction-table tbody tr:hover {
  background-color: #f5f7fa;
}

/* 表格内按钮样式 */
.table-btn {
  padding: 0;
  font-size: 13px;
  color: #409eff;
}

.table-btn:hover {
  text-decoration: underline;
}
</style>
