<template>
  <el-main>
    <el-button style="
        position: absolute;
        right: 0;
        margin-right: 30px;
        margin-top: 10px;
        z-index: 3;
      " icon="el-icon-full-screen" @click="fullscreen = !fullscreen" circle></el-button>

    <el-card v-if="!fullscreen" :class="`${cssPrefix}-monitor-container`">
      <!-- 顶部区域：表单 + 快捷按钮 -->
      <div class="header-section">
        <cm-grid :value="headerData"></cm-grid>
      </div>

      <!-- 信息区域：描述信息 -->
      <div class="info-section">
        <cm-grid :value="infoData"></cm-grid>
      </div>

      <!-- 备注区域：统一的备注展示 -->
      <div class="remarks-section">
        <el-row :gutter="20">
          <el-col :span="12" v-for="(remark, index) in remarks" :key="index">
            <el-card shadow="never" class="remark-card">
              <div slot="header" class="remark-header">
                <span>{{ remark.title }}</span>
              </div>
              <div class="remark-content">{{ remark.content }}</div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 表格区域：左右交替布局 -->
      <div class="tables-section">
        <el-row :gutter="20" v-for="(rowTables, rowIndex) in tableRows" :key="rowIndex" class="table-row">
          <el-col :span="12" v-for="(table, colIndex) in rowTables" :key="colIndex">
            <div class="table-wrapper">
              <cm-grid :value="[[table]]"></cm-grid>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 对话框 -->
      <el-dialog :visible.sync="visible" width="400px" title="提交">
        是否确认提交？{{ formData || "no data" }}
        <span slot="footer">
          <el-button plain size="small" @click="visible = false">取消</el-button>
          <el-button size="small" type="primary" @click="visible = false">确认</el-button>
        </span>
      </el-dialog>
    </el-card>

    <el-dialog :visible.sync="fullscreen" class="fullscreen-dialog" show-close>
      <div :class="`${cssPrefix}-monitor-container fullscreen-content`">
        <!-- 顶部区域：表单 + 快捷按钮 -->
        <div class="header-section">
          <cm-grid :value="headerData"></cm-grid>
        </div>

        <!-- 信息区域：描述信息 -->
        <div class="info-section">
          <cm-grid :value="infoData"></cm-grid>
        </div>

        <!-- 备注区域：统一的备注展示 -->
        <div class="remarks-section">
          <el-row :gutter="20">
            <el-col :span="12" v-for="(remark, index) in remarks" :key="index">
              <el-card shadow="never" class="remark-card">
                <div slot="header" class="remark-header">
                  <span>{{ remark.title }}</span>
                </div>
                <div class="remark-content">{{ remark.content }}</div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 表格区域：左右交替布局 -->
        <div class="tables-section">
          <el-row :gutter="20" v-for="(rowTables, rowIndex) in tableRows" :key="rowIndex" class="table-row">
            <el-col :span="12" v-for="(table, colIndex) in rowTables" :key="colIndex">
              <div class="table-wrapper">
                <cm-grid :value="[[table]]"></cm-grid>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 对话框 -->
        <el-dialog :visible.sync="visible" width="400px" title="提交">
          是否确认提交？{{ formData || "no data" }}
          <span slot="footer">
            <el-button plain size="small" @click="visible = false">取消</el-button>
            <el-button size="small" type="primary" @click="visible = false">确认</el-button>
          </span>
        </el-dialog>
      </div>
    </el-dialog>
  </el-main>
</template>

<script>
import CmGrid from '@/components/CompassMonitor/CmGrid/index.js'
import { getConfigByKey } from "@/api/monitor/config";
import { getMonitorDataWithParams, getSelectOptions } from "@/api/monitor/data";

export default {
  name: 'CommonMonitor',
  components: {
    CmGrid
  },
  props: {
    // 监控配置KEY（必填）
    configKey: {
      type: String,
      required: true
    },
    // 监控名称（用于错误提示）
    monitorName: {
      type: String,
      default: '监控'
    },
    // CSS类名前缀（用于样式隔离）
    cssPrefix: {
      type: String,
      default: 'common'
    },
    // descriptions 的列数
    descColumn: {
      type: Number,
      default: 4
    }
  },
  data() {
    return {
      fullscreen: false,
      visible: false,
      formData: null,
      loading: true,

      // 动态配置数据
      config: null,

      // 页面数据（从配置动态生成）
      headerData: [[]],
      infoData: [[]],
      remarks: [],
      tables: []
    };
  },
  created() {
    this.loadConfig();
  },
  computed: {
    // 将表格数据按左右布局分组
    tableRows() {
      const rows = [];
      for (let i = 0; i < this.tables.length; i += 2) {
        const row = [this.tables[i]];
        if (i + 1 < this.tables.length) {
          row.push(this.tables[i + 1]);
        }
        rows.push(row);
      }
      return rows;
    }
  },
  methods: {
    /** 加载监控配置 */
    loadConfig() {
      this.loading = true;
      getConfigByKey(this.configKey).then(response => {
        if (response.data && response.data.configJson) {
          this.config = JSON.parse(response.data.configJson);
          this.buildMonitorUI();
        }
        this.loading = false;
      }).catch(() => {
        this.loading = false;
        this.$message.error(`加载${this.monitorName}配置失败`);
      });
    },

    /** 根据配置构建监控UI */
    buildMonitorUI() {
      if (!this.config) return;

      // 构建headerData（表单+快捷按钮）
      if (this.config.formItems || this.config.widgetItems) {
        this.headerData = [[]]

        if (this.config.formItems && this.config.formItems.length > 0) {
          this.headerData[0].push({
            span: 16,
            type: "form",
            inline: true,
            items: this.config.formItems,
            onChange: this.handleFormChange,
            onEnter: this.handleEnter,
            onSubmit: this.handleQuery
          });
        }

        if (this.config.widgetItems && this.config.widgetItems.length > 0) {
          this.headerData[0].push({
            span: 8,
            type: "widget",
            items: this.config.widgetItems
          });
        }
      }

      // 构建infoData（基础信息）- 初始为空
      if (this.config.descItems && this.config.descItems.length > 0) {
        this.infoData = [[{
          span: 24,
          type: "descriptions",
          column: this.descColumn,
          border: true,
          items: this.config.descItems.map(item => ({
            label: item.label,
            value: ""
          }))
        }]];
      }

      // 构建remarks（备注）- 初始为空
      if (this.config.remarkItems && this.config.remarkItems.length > 0) {
        this.remarks = this.config.remarkItems.map(item => ({
          title: item.title,
          content: ""
        }));
      }

      // 构建tables（表格）- 初始为空
      if (this.config.tableConfigs && this.config.tableConfigs.length > 0) {
        this.tables = this.config.tableConfigs.map(tableConfig => {
          // 将配置格式转换为渲染格式
          const renderRows = this.convertConfigToRenderRows(tableConfig.rows || []);

          return {
            span: 24,
            type: "table",
            body: {
              rowHeader: tableConfig.rowHeader,
              rows: renderRows.map(row => ({
                projectName: row.projectName,
                projectNameRowSpan: row.projectNameRowSpan,
                subName: row.subName,
                unit: row.unit,
                value: ""
              }))
            }
          };
        });
      }
    },

    /** 处理表单变化 - 仅更新formData */
    handleFormChange(formData) {
      console.log('=== handleFormChange 触发 ===', formData);
      this.formData = formData;
    },

    /** 处理回车键 - 获取下拉框选项或自动查询 */
    handleEnter(formData) {
      console.log('=== handleEnter 触发（用户按下回车） ===', formData);
      this.formData = formData;

      // 检查是否需要获取下拉框选项
      const needFetchOptions = this.checkNeedFetchSelectOptions(formData);
      console.log('是否需要获取下拉框选项:', needFetchOptions);

      if (needFetchOptions) {
        console.log('开始获取下拉框选项...');
        this.fetchSelectOptions(formData);
        return;
      }

      // 检查是否所有必填字段都已填写
      if (this.config && this.config.formItems) {
        const allFilled = this.config.formItems.every(item => {
          const value = formData[item.prop];
          return value !== null && value !== undefined && value !== '';
        });

        console.log('所有字段是否填写完成:', allFilled);

        // 如果所有字段都填写完成，自动查询
        if (allFilled) {
          this.handleQuery(formData);
        }
      }
    },

    /**
     * 检查是否需要获取下拉框选项
     * 条件：所有input已填写 && 有select未加载选项 && select的dataSource是xml
     */
    checkNeedFetchSelectOptions(formData) {
      if (!this.config || !this.config.formItems) {
        console.log('配置不存在');
        return false;
      }

      // 查找input类型的字段
      const inputItems = this.config.formItems.filter(item => item.type === 'input');
      console.log('input类型的字段:', inputItems);

      // 检查所有input是否都已填写
      const allInputFilled = inputItems.every(item => {
        const value = formData[item.prop];
        const filled = value !== null && value !== undefined && value !== '';
        console.log(`  ${item.prop}: "${value}" -> ${filled ? '已填写' : '未填写'}`);
        return filled;
      });
      console.log('所有input是否都已填写:', allInputFilled);

      // 检查是否有select类型且未加载选项（并且dataSource是xml）
      const hasEmptySelect = this.config.formItems.some(item => {
        if (item.type === 'select' && item.dataSource === 'xml') {
          const empty = !item.options || item.options.length === 0;
          console.log(`  ${item.prop} (select): options长度=${item.options ? item.options.length : 0}, empty=${empty}`);
          return empty;
        }
        return false;
      });
      console.log('是否有需要从XML加载的空select:', hasEmptySelect);

      return allInputFilled && hasEmptySelect;
    },

    /**
     * 获取下拉框选项
     */
    fetchSelectOptions(formData) {
      this.loading = true;
      console.log('发送请求获取下拉框选项:', this.configKey, formData);

      // 调用后端接口
      getSelectOptions(this.configKey, formData)
        .then(response => {
          console.log('下拉框选项响应:', response);
          if (response.code === 200) {
            const options = response.data;
            console.log('解析到的选项:', options);

            // 更新formItems中的options
            this.config.formItems.forEach(item => {
              if (item.type === 'select' && options[item.prop]) {
                console.log(`为 ${item.prop} 设置选项:`, options[item.prop]);
                this.$set(item, 'options', options[item.prop].map(val => ({
                  label: val,
                  value: val
                })));
              }
            });

            // 重新构建UI
            this.buildMonitorUI();
          }
          this.loading = false;
        })
        .catch(error => {
          console.error('获取下拉框选项失败:', error);
          this.loading = false;
          this.$message.error('获取下拉框选项失败');
        });
    },

    /** 处理查询 */
    handleQuery(formData) {
      this.formData = formData;
      this.loading = true;

      // 构建请求参数，包含下拉框的配置信息
      const requestData = {
        ...formData,
        // 新增：下拉框配置映射
        selectConfigs: this.getSelectConfigs()
      };

      getMonitorDataWithParams(this.configKey, requestData).then(response => {
        if (response.data) {
          this.updateMonitorData(response.data);
        }
        this.loading = false;
      }).catch(() => {
        this.loading = false;
        this.$message.error(`查询${this.monitorName}数据失败`);
      });
    },

    /**
     * 获取下拉框配置信息
     */
    getSelectConfigs() {
      const configs = {};

      if (this.config && this.config.formItems) {
        this.config.formItems.forEach(item => {
          if (item.type === 'select' && item.dataSource === 'xml') {
            configs[item.prop] = {
              expression: item.expression  // "001;2;2"
            };
          }
        });
      }

      return configs;
    },

    /** 将配置格式转换为渲染格式 */
    convertConfigToRenderRows(configRows) {
      const renderRows = [];

      configRows.forEach(row => {
        if (row.rowType === 'simple') {
          // 简单行
          renderRows.push({
            projectName: row.projectName || '',
            projectNameRowSpan: undefined,
            subName: '',
            unit: row.unit || '',
            dataSource: row.dataSource || 'database',
            displayType: row.displayType || 'direct',
            expression: row.expression || '',
            value: row.value || ''
          });
        } else if (row.rowType === 'complex') {
          // 复杂行
          const subRows = row.subRows || [];
          subRows.forEach((subRow, subIndex) => {
            renderRows.push({
              projectName: subIndex === 0 ? row.projectName : '',
              projectNameRowSpan: subIndex === 0 ? subRows.length : 0,
              subName: subRow.subName || '',
              unit: subRow.unit || '',
              dataSource: subRow.dataSource || 'database',
              displayType: subRow.displayType || 'direct',
              expression: subRow.expression || '',
              value: subRow.value || ''
            });
          });
        }
      });

      return renderRows;
    },

    /** 更新监控数据 */
    updateMonitorData(data) {
      // 更新基础信息
      if (data.descItems && this.infoData[0] && this.infoData[0][0]) {
        this.infoData[0][0].items = data.descItems.map(item => ({
          label: item.label,
          value: item.value || '-'
        }));
      }

      // 更新备注
      if (data.remarkItems) {
        this.remarks = data.remarkItems.map(item => ({
          title: item.title,
          content: item.content || '-'
        }));
      }

      // 更新表格数据
      if (data.tableConfigs && this.tables.length > 0) {
        data.tableConfigs.forEach((tableConfig, index) => {
          if (this.tables[index] && tableConfig.rows) {
            // 将配置格式转换为渲染格式
            const renderRows = this.convertConfigToRenderRows(tableConfig.rows);
            this.tables[index].body.rows = renderRows.map(row => ({
              projectName: row.projectName,
              projectNameRowSpan: row.projectNameRowSpan,
              subName: row.subName,
              unit: row.unit,
              value: row.value || '-'
            }));
          }
        });
      }
    }
  }
};
</script>

<style scoped>
/* 容器样式 - 使用动态类名 */
.common-monitor-container,
.lcp-monitor-container,
.gp-monitor-container {
  padding: 20px;
}

/* 区域间距 */
.header-section {
  margin-bottom: 20px;
}

.info-section {
  margin-bottom: 20px;
}

.remarks-section {
  margin-bottom: 30px;
}

.tables-section {
  margin-top: 20px;
}

/* 备注区域样式 */
.remarks-section {
  padding: 10px 0;
}

.remark-card {
  height: 100%;
  background-color: #fafbfc;
}

.remark-card >>> .el-card__header {
  padding: 12px 20px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e6ebf5;
}

.remark-header {
  font-weight: 500;
  color: #333b5b;
  font-size: 14px;
}

.remark-content {
  padding: 10px 0;
  line-height: 1.8;
  color: #606266;
  word-wrap: break-word;
  word-break: break-all;
  max-height: 150px;
  overflow-y: auto;
}

/* 表格区域样式 */
.table-row {
  margin-bottom: 20px;
}

.table-wrapper {
  background: #fff;
  border: 1px solid #e6ebf5;
  border-radius: 4px;
  overflow: hidden;
  transition: box-shadow 0.3s;
}

.table-wrapper:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.table-wrapper >>> .cm-table {
  padding: 0;
}

.table-wrapper >>> .el-descriptions__table {
  margin: 0;
}

/* 全屏模式样式 */
.fullscreen-content {
  padding: 20px;
  height: 100%;
  overflow-y: auto;
}

/* 滚动条美化 */
.remark-content::-webkit-scrollbar,
.fullscreen-content::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.remark-content::-webkit-scrollbar-thumb,
.fullscreen-content::-webkit-scrollbar-thumb {
  background-color: #c1ccbc;
  border-radius: 3px;
}

.remark-content::-webkit-scrollbar-track,
.fullscreen-content::-webkit-scrollbar-track {
  background-color: #f5f7fa;
}

/* 响应式优化 */
@media screen and (max-width: 1200px) {
  .table-row >>> .el-col-12 {
    width: 100%;
    margin-bottom: 20px;
  }

  .remarks-section >>> .el-col-12 {
    width: 100%;
    margin-bottom: 15px;
  }
}

.fullscreen-dialog {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 2000;
}

.fullscreen-dialog >>> .el-dialog {
  margin: 0;
  width: 100vw;
  height: 100vh;
  max-width: none;
  border-radius: 0;
}

.fullscreen-dialog >>> .el-dialog__body {
  padding: 0;
  height: 100vh;
  overflow: hidden;
}

.fullscreen-dialog >>> .el-dialog__header {
  display: none;
}
</style>

