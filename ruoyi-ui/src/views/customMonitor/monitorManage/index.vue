<template>
  <div class="monitor-manage-container">
    <!-- 顶部表单区域 -->
    <el-card class="form-card">
      <el-row :gutter="20">
        <!-- 右上角按钮 -->
        <el-col :span="24" style="text-align: right; padding-top: 10px;">
          <el-button size="small" type="primary" icon="el-icon-plus" @click="handleAdd">新增监控页面</el-button>
          <el-button size="small" type="danger" icon="el-icon-delete" @click="handleBatchDelete">批量删除监控页面</el-button>
        </el-col>
      </el-row>
    </el-card>
  
    <!-- 表格区域 -->
    <el-card class="table-card" style="margin-top: 20px;">
      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
        height="450"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
        <el-table-column prop="code" label="监控页面KEY" min-width="150" align="center"></el-table-column>
        <el-table-column prop="name" label="监控页面名称" min-width="180" align="center"></el-table-column>
        <el-table-column prop="status" label="状态" min-width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === '启用' ? 'success' : 'info'" size="small">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" align="center"></el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-edit" @click="handleRowEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete" @click="handleRowDelete(scope.row)" style="color: #F56C6C;">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        style="margin-top: 20px; text-align: right;"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="queryParams.pageNum"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="queryParams.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
      >
      </el-pagination>
    </el-card>

    <!-- 新增/编辑监控页面对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="90%"
      top="5vh"
      :close-on-click-modal="false"
      class="monitor-config-dialog"
    >
      <el-form ref="monitorForm" :model="monitorForm" label-width="120px" size="small">
        <!-- 基本信息 -->
        <el-card shadow="never" class="section-card">
          <div slot="header" class="section-header">
            <i class="el-icon-info"></i>
            <span>基本信息</span>
          </div>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="监控页面KEY" prop="configKey">
                <el-input v-model="monitorForm.configKey" placeholder="请输入英文KEY，如：gpMonitor"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="监控页面名称" prop="configName">
                <el-input v-model="monitorForm.configName" placeholder="请输入中文名称，如：GP监控"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="API地址" prop="apiUrl">
                <el-input v-model="monitorForm.apiUrl" placeholder="请输入外部API地址（可选），如：https://jsonplaceholder.typicode.com/users/1">
                  <template slot="prepend">
                    <i class="el-icon-link"></i>
                  </template>
                </el-input>
                <div style="color: #909399; font-size: 12px; margin-top: 5px;">
                  <i class="el-icon-info"></i> 配置API数据源时，将从此地址获取数据。留空则API数据源无法使用。
                </div>
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>

        <!-- 输入部分配置 -->
        <el-card shadow="never" class="section-card">
          <div slot="header" class="section-header">
            <i class="el-icon-edit"></i>
            <span>输入部分配置（Form表单）</span>
            <el-button size="mini" type="primary" icon="el-icon-plus" style="float: right;" @click="addFormItem">新增输入项</el-button>
          </div>
          <el-table :data="monitorForm.formItems" border size="small" max-height="300">
            <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
            <el-table-column label="中文标签" min-width="150">
              <template slot-scope="scope">
                <el-input v-model="scope.row.label" placeholder="如：GP品名"></el-input>
              </template>
            </el-table-column>
            <el-table-column label="英文字段" min-width="150">
              <template slot-scope="scope">
                <el-input v-model="scope.row.prop" placeholder="如：gpName"></el-input>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template slot-scope="scope">
                <el-button size="mini" type="text" icon="el-icon-delete" @click="deleteFormItem(scope.$index)" style="color: #F56C6C;">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 链接按钮部分配置 -->
        <el-card shadow="never" class="section-card">
          <div slot="header" class="section-header">
            <i class="el-icon-link"></i>
            <span>链接按钮部分配置（Widget）</span>
            <el-button size="mini" type="primary" icon="el-icon-plus" style="float: right;" @click="addWidgetItem">新增链接按钮</el-button>
          </div>
          <el-table :data="monitorForm.widgetItems" border size="small" max-height="300">
            <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
            <el-table-column label="按钮文字" min-width="150">
              <template slot-scope="scope">
                <el-input v-model="scope.row.text" placeholder="如：GP加工标准"></el-input>
              </template>
            </el-table-column>
            <el-table-column label="链接URL" min-width="200">
              <template slot-scope="scope">
                <el-input v-model="scope.row.href" placeholder="如：http://example.com"></el-input>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template slot-scope="scope">
                <el-button size="mini" type="text" icon="el-icon-delete" @click="deleteWidgetItem(scope.$index)" style="color: #F56C6C;">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 基础信息部分配置 -->
        <el-card shadow="never" class="section-card">
          <div slot="header" class="section-header">
            <i class="el-icon-document"></i>
            <span>基础信息部分配置（Descriptions）</span>
            <el-button size="mini" type="primary" icon="el-icon-plus" style="float: right;" @click="addDescItem">新增信息项</el-button>
          </div>
          <el-table :data="monitorForm.descItems" border size="small" max-height="300">
            <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
            <el-table-column label="中文标签" min-width="120">
              <template slot-scope="scope">
                <el-input v-model="scope.row.label" placeholder="如：作业员"></el-input>
              </template>
            </el-table-column>
            <el-table-column label="数据源" min-width="120">
              <template slot-scope="scope">
                <el-select v-model="scope.row.dataSource" placeholder="请选择" @change="handleDataSourceChange(scope.row, 'desc')">
                  <el-option label="API获取" value="api"></el-option>
                  <el-option label="数据库获取" value="database"></el-option>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="显示类型" min-width="120">
              <template slot-scope="scope">
                <el-select
                  v-model="scope.row.displayType"
                  placeholder="请选择"
                >
                  <el-option label="直接显示" value="direct"></el-option>
                  <el-option label="运算后显示" value="computed"></el-option>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="变量名" min-width="100">
              <template slot-scope="scope">
                <el-input
                  v-model="scope.row.valueKey"
                  placeholder="如：avgAge"
                  v-if="scope.row.dataSource === 'database'"
                ></el-input>
                <span v-else style="color: #909399; font-size: 12px;">-</span>
              </template>
            </el-table-column>
            <el-table-column label="表达式/字段" min-width="200">
              <template slot-scope="scope">
                <el-input
                  v-model="scope.row.expression"
                  type="textarea"
                  :rows="2"
                  :placeholder="getExpressionPlaceholder(scope.row)"
                ></el-input>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template slot-scope="scope">
                <el-button size="mini" type="text" icon="el-icon-delete" @click="deleteDescItem(scope.$index)" style="color: #F56C6C;">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 备注部分配置 -->
        <el-card shadow="never" class="section-card">
          <div slot="header" class="section-header">
            <i class="el-icon-chat-line-square"></i>
            <span>备注部分配置</span>
            <el-button size="mini" type="primary" icon="el-icon-plus" style="float: right;" @click="addRemarkItem">新增备注项</el-button>
          </div>
          <el-table :data="monitorForm.remarkItems" border size="small" max-height="300">
            <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
            <el-table-column label="备注标题" min-width="120">
              <template slot-scope="scope">
                <el-input v-model="scope.row.title" placeholder="如：备注1"></el-input>
              </template>
            </el-table-column>
            <el-table-column label="数据源" min-width="120">
              <template slot-scope="scope">
                <el-select v-model="scope.row.dataSource" placeholder="请选择" @change="handleDataSourceChange(scope.row, 'remark')">
                  <el-option label="API获取" value="api"></el-option>
                  <el-option label="数据库获取" value="database"></el-option>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="显示类型" min-width="120">
              <template slot-scope="scope">
                <el-select
                  v-model="scope.row.displayType"
                  placeholder="请选择"
                >
                  <el-option label="直接显示" value="direct"></el-option>
                  <el-option label="运算后显示" value="computed"></el-option>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="变量名" min-width="100">
              <template slot-scope="scope">
                <el-input
                  v-model="scope.row.valueKey"
                  placeholder="如：avgAge"
                  v-if="scope.row.dataSource === 'database'"
                ></el-input>
                <span v-else style="color: #909399; font-size: 12px;">-</span>
              </template>
            </el-table-column>
            <el-table-column label="表达式/字段" min-width="200">
              <template slot-scope="scope">
                <el-input
                  v-model="scope.row.expression"
                  type="textarea"
                  :rows="2"
                  :placeholder="getExpressionPlaceholder(scope.row)"
                ></el-input>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template slot-scope="scope">
                <el-button size="mini" type="text" icon="el-icon-delete" @click="deleteRemarkItem(scope.$index)" style="color: #F56C6C;">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- Table部分配置 - 全新设计 -->
        <el-card shadow="never" class="section-card">
          <div slot="header" class="section-header">
            <i class="el-icon-s-grid"></i>
            <span>Table部分配置</span>
            <el-button size="mini" type="primary" icon="el-icon-plus" style="float: right;" @click="addTableConfig">新增Table</el-button>
          </div>

          <!-- Table列表 -->
          <el-collapse v-model="activeTableNames" accordion>
            <el-collapse-item
              v-for="(table, tableIndex) in monitorForm.tableConfigs"
              :key="tableIndex"
              :name="tableIndex"
            >
              <template slot="title">
                <div style="width: 100%; display: flex; justify-content: space-between; align-items: center;">
                  <span><i class="el-icon-s-grid"></i> Table {{ tableIndex + 1 }}: {{ table.rowHeader || '未命名' }}</span>
                  <el-button
                    size="mini"
                    type="text"
                    icon="el-icon-delete"
                    @click.stop="deleteTableConfig(tableIndex)"
                    style="color: #F56C6C; margin-right: 20px;"
                  >
                    删除此Table
                  </el-button>
                </div>
              </template>

              <!-- Table基本配置 -->
              <el-form-item label="表格行头" label-width="100px">
                <el-input v-model="table.rowHeader" placeholder="如：印刷检查、面积检查" style="width: 300px;"></el-input>
              </el-form-item>

              <!-- 行配置 -->
              <div style="margin-top: 15px;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
                  <span style="font-weight: 500; color: #606266;">行配置</span>
                  <el-button size="mini" type="primary" icon="el-icon-plus" @click="addTableRow(tableIndex)">
                    新增行
                  </el-button>
                </div>

                <!-- 行列表 -->
                <div v-for="(row, rowIndex) in table.rows" :key="rowIndex" style="margin-bottom: 15px;">
                  <el-card shadow="hover">
                    <div slot="header" style="display: flex; justify-content: space-between; align-items: center;">
                      <span><strong>行 {{ rowIndex + 1 }}</strong></span>
                      <div>
                        <el-button
                          size="mini"
                          type="text"
                          icon="el-icon-top"
                          @click="moveTableRow(tableIndex, rowIndex, 'up')"
                          :disabled="rowIndex === 0"
                        >
                          上移
                        </el-button>
                        <el-button
                          size="mini"
                          type="text"
                          icon="el-icon-bottom"
                          @click="moveTableRow(tableIndex, rowIndex, 'down')"
                          :disabled="rowIndex === table.rows.length - 1"
                        >
                          下移
                        </el-button>
                        <el-button
                          size="mini"
                          type="text"
                          icon="el-icon-delete"
                          @click="deleteTableRow(tableIndex, rowIndex)"
                          style="color: #F56C6C;"
                        >
                          删除
                        </el-button>
                      </div>
                    </div>

                    <!-- 行类型选择 -->
                    <el-form-item label="行类型" label-width="80px">
                      <el-radio-group v-model="row.rowType" @change="handleRowTypeChange(tableIndex, rowIndex)">
                        <el-radio label="simple">简单行（单项目）</el-radio>
                        <el-radio label="complex">复杂行（带子项）</el-radio>
                      </el-radio-group>
                    </el-form-item>

                    <!-- 简单行配置 -->
                    <div v-if="row.rowType === 'simple'">
                      <el-row :gutter="10">
                        <el-col :span="8">
                          <el-form-item label="项目名" label-width="80px">
                            <el-input v-model="row.projectName" placeholder="如：LCP处置项次"></el-input>
                          </el-form-item>
                        </el-col>
                        <el-col :span="8">
                          <el-form-item label="单位" label-width="80px">
                            <el-input v-model="row.unit" placeholder="如：m/min（可选）"></el-input>
                          </el-form-item>
                        </el-col>
                        <el-col :span="8">
                          <el-form-item label="数据源" label-width="80px">
                            <el-select v-model="row.dataSource" placeholder="请选择" size="small" @change="handleDataSourceChange(row, 'tableRow')">
                              <el-option label="API" value="api"></el-option>
                              <el-option label="数据库" value="database"></el-option>
                            </el-select>
                          </el-form-item>
                        </el-col>
                      </el-row>
                      <el-row :gutter="10">
                        <el-col :span="8">
                          <el-form-item label="显示类型" label-width="80px">
                            <el-select
                              v-model="row.displayType"
                              placeholder="请选择"
                              size="small"
                            >
                              <el-option label="直接显示" value="direct"></el-option>
                              <el-option label="运算" value="computed"></el-option>
                            </el-select>
                          </el-form-item>
                        </el-col>
                        <el-col :span="8">
                          <el-form-item label="变量名" label-width="80px">
                            <el-input
                              v-model="row.valueKey"
                              placeholder="如：avgAge"
                              v-if="row.dataSource === 'database'"
                              size="small"
                            ></el-input>
                            <span v-else style="color: #909399; font-size: 12px;">-</span>
                          </el-form-item>
                        </el-col>
                        <el-col :span="8">
                          <el-form-item label="表达式/字段" label-width="80px">
                            <el-input v-model="row.expression" :placeholder="getExpressionPlaceholder(row)" size="small"></el-input>
                          </el-form-item>
                        </el-col>
                      </el-row>
                    </div>

                    <!-- 复杂行配置 -->
                    <div v-if="row.rowType === 'complex'">
                      <el-form-item label="主项目名" label-width="80px">
                        <el-input v-model="row.projectName" placeholder="如：基准精度/尺度" style="width: 300px;"></el-input>
                      </el-form-item>

                      <div style="display: flex; justify-content: space-between; align-items: center; margin: 10px 0;">
                        <span style="font-weight: 500; color: #606266;">子项配置</span>
                        <el-button size="mini" type="primary" plain icon="el-icon-plus" @click="addSubRow(tableIndex, rowIndex)">
                          新增子项
                        </el-button>
                      </div>

                      <!-- 子项列表 -->
                      <el-table :data="row.subRows" border size="small" max-height="300">
                        <el-table-column type="index" label="#" width="50" align="center"></el-table-column>

                        <el-table-column label="子项名" min-width="100">
                          <template slot-scope="scope">
                            <el-input v-model="scope.row.subName" placeholder="如：L、W" size="small"></el-input>
                          </template>
                        </el-table-column>

                        <el-table-column label="单位" min-width="80">
                          <template slot-scope="scope">
                            <el-input v-model="scope.row.unit" placeholder="如：um" size="small"></el-input>
                          </template>
                        </el-table-column>

                        <el-table-column label="数据源" min-width="100">
                          <template slot-scope="scope">
                            <el-select v-model="scope.row.dataSource" placeholder="请选择" size="small" @change="handleDataSourceChange(scope.row, 'subRow')">
                              <el-option label="API" value="api"></el-option>
                              <el-option label="数据库" value="database"></el-option>
                            </el-select>
                          </template>
                        </el-table-column>

                        <el-table-column label="显示类型" min-width="100">
                          <template slot-scope="scope">
                            <el-select
                              v-model="scope.row.displayType"
                              placeholder="请选择"
                              size="small"
                            >
                              <el-option label="直接显示" value="direct"></el-option>
                              <el-option label="运算" value="computed"></el-option>
                            </el-select>
                          </template>
                        </el-table-column>

                        <el-table-column label="变量名" min-width="100">
                          <template slot-scope="scope">
                            <el-input
                              v-model="scope.row.valueKey"
                              placeholder="如：avgAge"
                              size="small"
                              v-if="scope.row.dataSource === 'database'"
                            ></el-input>
                            <span v-else style="color: #909399; font-size: 12px;">-</span>
                          </template>
                        </el-table-column>

                        <el-table-column label="表达式/字段" min-width="150">
                          <template slot-scope="scope">
                            <el-input v-model="scope.row.expression" :placeholder="getExpressionPlaceholder(scope.row)" size="small"></el-input>
                          </template>
                        </el-table-column>

                        <el-table-column label="操作" width="80" align="center" fixed="right">
                          <template slot-scope="scope">
                            <el-button
                              size="mini"
                              type="text"
                              icon="el-icon-delete"
                              @click="deleteSubRow(tableIndex, rowIndex, scope.$index)"
                              style="color: #F56C6C;"
                            >
                              删除
                            </el-button>
                          </template>
                        </el-table-column>
                      </el-table>
                    </div>
                  </el-card>
                </div>

                <!-- 空状态提示 -->
                <el-empty v-if="!table.rows || table.rows.length === 0" description="暂无行，请点击上方按钮新增"></el-empty>
              </div>
            </el-collapse-item>
          </el-collapse>
        </el-card>
      </el-form>

      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSubmit">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { listConfig, getConfig, addConfig, updateConfig, delConfig } from "@/api/monitor/config";

export default {
  name: 'MonitorManage',
  data() {
    return {
      // 加载状态
      loading: true,

      // 对话框相关
      dialogVisible: false,
      dialogTitle: '新增监控页面',
      isEdit: false,
      editId: null,

      // 监控页面表单数据
      monitorForm: {
        configKey: '',
        configName: '',
        apiUrl: '',  // 新增：API地址
        formItems: [],
        widgetItems: [],
        descItems: [],
        remarkItems: [],
        tableConfigs: []  // 新结构：[{ rowHeader: '', rows: [] }]
      },

      // Table折叠面板激活项
      activeTableNames: 0,

      // 表格选中数据
      selectedRows: [],

      // 表格数据
      tableData: [],

      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        configKey: null,
        configName: null,
        status: null
      },

      // 总条数
      total: 0
    };
  },
  created() {
    this.getList();
  },
  methods: {
    // ==================== 数据加载 ====================

    /** 查询监控配置列表 */
    getList() {
      this.loading = true;
      listConfig(this.queryParams).then(response => {
        this.tableData = response.rows.map(item => {
          return {
            configId: item.configId,
            code: item.configKey,
            name: item.configName,
            status: item.status === '0' ? '启用' : '停用',
            createTime: item.createTime,
            config: item.configJson ? JSON.parse(item.configJson) : null
          };
        });
        this.total = response.total;
        this.loading = false;
      });
    },

    // ==================== 主表格操作 ====================

    // 表格选择改变
    handleSelectionChange(selection) {
      this.selectedRows = selection;
    },

    // 新增监控页面按钮
    handleAdd() {
      this.dialogTitle = '新增监控页面';
      this.isEdit = false;
      this.editId = null;
      this.resetMonitorForm();
      this.dialogVisible = true;
    },

    // 批量删除监控页面按钮
    handleBatchDelete() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请先选择要删除的监控页面');
        return;
      }
      const configIds = this.selectedRows.map(item => item.configId);
      this.$confirm(`确认删除选中的 ${this.selectedRows.length} 个监控页面？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return delConfig(configIds);
      }).then(() => {
        this.getList();
        this.$message.success('删除成功');
      }).catch(() => {});
    },

    // 表格行编辑
    handleRowEdit(row) {
      this.dialogTitle = '编辑监控页面';
      this.isEdit = true;
      this.editId = row.configId;

      // 加载配置数据
      getConfig(row.configId).then(response => {
        const config = response.data.configJson ? JSON.parse(response.data.configJson) : {};

        // 处理 tableConfigs 的数据结构
        let tableConfigs = [];
        if (config.tableConfigs && config.tableConfigs.length > 0) {
          tableConfigs = config.tableConfigs.map(tableConfig => {
            // 检查是否是新版结构（有rows字段且rows[0]有rowType）
            if (tableConfig.rows && tableConfig.rows.length > 0 && tableConfig.rows[0].rowType) {
              return {
                rowHeader: tableConfig.rowHeader || '',
                rows: tableConfig.rows || []
              };
            }
            // 默认返回空的新结构
            return {
              rowHeader: tableConfig.rowHeader || '',
              rows: []
            };
          });
        }

        this.monitorForm = {
          configKey: response.data.configKey,
          configName: response.data.configName,
          apiUrl: config.apiUrl || '',  // 加载API地址
          formItems: config.formItems || [],
          widgetItems: config.widgetItems || [],
          descItems: config.descItems || [],
          remarkItems: config.remarkItems || [],
          tableConfigs: tableConfigs
        };
        this.dialogVisible = true;
      });
    },



    // 表格行删除
    handleRowDelete(row) {
      this.$confirm(`确认删除监控页面"${row.name}"？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return delConfig(row.configId);
      }).then(() => {
        this.getList();
        this.$message.success('删除成功');
      }).catch(() => {});
    },

    // 分页大小改变
    handleSizeChange(val) {
      this.queryParams.pageSize = val;
      this.getList();
    },

    // 当前页改变
    handleCurrentChange(val) {
      this.queryParams.pageNum = val;
      this.getList();
    },

    // ==================== 表单项操作 ====================

    // 新增表单输入项
    addFormItem() {
      this.monitorForm.formItems.push({
        label: '',
        prop: ''
      });
    },

    // 删除表单输入项
    deleteFormItem(index) {
      this.monitorForm.formItems.splice(index, 1);
    },

    // ==================== Widget操作 ====================

    // 新增Widget链接按钮
    addWidgetItem() {
      this.monitorForm.widgetItems.push({
        text: '',
        href: ''
      });
    },

    // 删除Widget链接按钮
    deleteWidgetItem(index) {
      this.monitorForm.widgetItems.splice(index, 1);
    },

    // ==================== Descriptions操作 ====================

    // 新增基础信息项
    addDescItem() {
      this.monitorForm.descItems.push({
        label: '',
        dataSource: 'api',
        displayType: 'direct',
        expression: '',
        valueKey: ''
      });
    },

    // 删除基础信息项
    deleteDescItem(index) {
      this.monitorForm.descItems.splice(index, 1);
    },

    // ==================== 备注操作 ====================

    // 新增备注项
    addRemarkItem() {
      this.monitorForm.remarkItems.push({
        title: '',
        dataSource: 'api',
        displayType: 'direct',
        expression: '',
        valueKey: ''
      });
    },

    // 删除备注项
    deleteRemarkItem(index) {
      this.monitorForm.remarkItems.splice(index, 1);
    },

    // ==================== 数据源变化处理 ====================

    // 当数据源改变时的处理（现在支持数据库数据源的计算）
    handleDataSourceChange(row) {
      // 数据源改变时，清空valueKey
      if (row.valueKey) {
        row.valueKey = '';
      }
    },

    // ==================== Table配置操作 ====================

    // 新增Table配置
    addTableConfig() {
      this.monitorForm.tableConfigs.push({
        rowHeader: '',
        rows: []
      });
      // 自动展开新增的Table
      this.activeTableNames = this.monitorForm.tableConfigs.length - 1;
    },

    // 删除Table配置
    deleteTableConfig(tableIndex) {
      this.$confirm('确认删除此Table配置？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.monitorForm.tableConfigs.splice(tableIndex, 1);
        this.$message.success('删除成功');
      }).catch(() => {});
    },

    // 新增行
    addTableRow(tableIndex) {
      this.monitorForm.tableConfigs[tableIndex].rows.push({
        rowType: 'simple',
        projectName: '',
        unit: '',
        dataSource: 'database',
        displayType: 'direct',
        expression: '',
        valueKey: '',
        subRows: []
      });
    },

    // 删除行
    deleteTableRow(tableIndex, rowIndex) {
      this.$confirm('确认删除此行？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.monitorForm.tableConfigs[tableIndex].rows.splice(rowIndex, 1);
        this.$message.success('删除成功');
      }).catch(() => {});
    },

    // 移动行
    moveTableRow(tableIndex, rowIndex, direction) {
      const rows = this.monitorForm.tableConfigs[tableIndex].rows;
      if (direction === 'up' && rowIndex > 0) {
        const temp = rows[rowIndex];
        this.$set(rows, rowIndex, rows[rowIndex - 1]);
        this.$set(rows, rowIndex - 1, temp);
      } else if (direction === 'down' && rowIndex < rows.length - 1) {
        const temp = rows[rowIndex];
        this.$set(rows, rowIndex, rows[rowIndex + 1]);
        this.$set(rows, rowIndex + 1, temp);
      }
    },

    // 行类型改变
    handleRowTypeChange(tableIndex, rowIndex) {
      const row = this.monitorForm.tableConfigs[tableIndex].rows[rowIndex];
      if (row.rowType === 'complex' && (!row.subRows || row.subRows.length === 0)) {
        // 切换到复杂类型时，如果没有子项，自动添加一个
        this.$set(row, 'subRows', [{
          subName: '',
          unit: '',
          dataSource: 'database',
          displayType: 'direct',
          expression: '',
          valueKey: ''
        }]);
      }
    },

    // 新增子行
    addSubRow(tableIndex, rowIndex) {
      const row = this.monitorForm.tableConfigs[tableIndex].rows[rowIndex];
      if (!row.subRows) {
        this.$set(row, 'subRows', []);
      }
      row.subRows.push({
        subName: '',
        unit: '',
        dataSource: 'database',
        displayType: 'direct',
        expression: '',
        valueKey: ''
      });
    },

    // 删除子行
    deleteSubRow(tableIndex, rowIndex, subIndex) {
      const row = this.monitorForm.tableConfigs[tableIndex].rows[rowIndex];
      if (row.subRows.length <= 1) {
        this.$message.warning('至少保留一个子项');
        return;
      }
      row.subRows.splice(subIndex, 1);
    },

    // ==================== 对话框操作 ====================

    // 重置表单
    resetMonitorForm() {
      this.monitorForm = {
        configKey: '',
        configName: '',
        apiUrl: '',  // 重置API地址
        formItems: [],
        widgetItems: [],
        descItems: [],
        remarkItems: [],
        tableConfigs: []
      };
      this.activeTableNames = 0;
    },

    // 提交表单
    handleSubmit() {
      // 验证必填项
      if (!this.monitorForm.configKey) {
        this.$message.warning('请输入监控页面KEY');
        return;
      }
      if (!this.monitorForm.configName) {
        this.$message.warning('请输入监控页面名称');
        return;
      }

      // 直接保存配置格式，不进行转换
      // 转换工作在前端展示页面加载时进行
      const configJson = {
        apiUrl: this.monitorForm.apiUrl,  // 保存API地址
        formItems: this.monitorForm.formItems,
        widgetItems: this.monitorForm.widgetItems,
        descItems: this.monitorForm.descItems,
        remarkItems: this.monitorForm.remarkItems,
        tableConfigs: this.monitorForm.tableConfigs
      };

      // 构建提交数据
      const data = {
        configKey: this.monitorForm.configKey,
        configName: this.monitorForm.configName,
        configJson: JSON.stringify(configJson),
        status: '0'
      };

      if (this.isEdit) {
        // 编辑模式
        data.configId = this.editId;
        updateConfig(data).then(() => {
          this.$message.success('修改成功');
          this.dialogVisible = false;
          this.getList();
        });
      } else {
        // 新增模式
        addConfig(data).then(() => {
          this.$message.success('新增成功');
          this.dialogVisible = false;
          this.getList();
        });
      }
    },



    // 获取表达式/字段的占位符提示
    getExpressionPlaceholder(row) {
      const dataSource = row.dataSource || 'api';
      const displayType = row.displayType || 'direct';

      if (dataSource === 'api') {
        // API 数据源
        if (displayType === 'computed') {
          return '如：id * 100 或 (lat + lng) / 2（引用API字段进行计算）';
        } else {
          return '如：name 或 address.city 或 geo.lat（支持嵌套路径）';
        }
      } else if (dataSource === 'database') {
        // 数据库数据源
        if (displayType === 'computed') {
          return '如：(maxAge + minAge) / 2（引用其他字段的变量名进行计算）';
        } else {
          return '如：SELECT MAX(age) FROM users WHERE name = :userName';
        }
      }

      return '请输入表达式或字段名';
    }
  }
};
</script>

<style scoped>
.monitor-manage-container {
  padding: 20px;
}

.form-card {
  background-color: #fff;
}

.table-card {
  background-color: #fff;
}

/* 表单项样式优化 */
.el-form-item {
  margin-bottom: 18px;
}

/* 表格样式优化 */
.el-table {
  font-size: 13px;
}

.el-table th {
  background-color: #f5f7fa;
  color: #333b5b;
  font-weight: 500;
}

/* ==================== 对话框样式 ==================== */
.monitor-config-dialog >>> .el-dialog__body {
  max-height: 75vh;
  overflow-y: auto;
  padding: 15px 20px;
}

/* 滚动条美化 */
.monitor-config-dialog >>> .el-dialog__body::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.monitor-config-dialog >>> .el-dialog__body::-webkit-scrollbar-thumb {
  background-color: #c1c1c1;
  border-radius: 4px;
}

.monitor-config-dialog >>> .el-dialog__body::-webkit-scrollbar-track {
  background-color: #f5f7fa;
}

/* 区域卡片样式 */
.section-card {
  margin-bottom: 20px;
  border: 1px solid #e6ebf5;
}

.section-card:last-child {
  margin-bottom: 0;
}

.section-card >>> .el-card__header {
  background-color: #f5f7fa;
  padding: 12px 20px;
  border-bottom: 1px solid #e6ebf5;
}

.section-card >>> .el-card__body {
  padding: 20px;
}

.section-header {
  display: flex;
  align-items: center;
  font-weight: 500;
  color: #333b5b;
  font-size: 14px;
}

.section-header i {
  margin-right: 8px;
  font-size: 16px;
  color: #409eff;
}

.section-header span {
  flex: 1;
}

/* 表格内输入框样式 */
.section-card >>> .el-table .el-input__inner {
  border: 1px solid #dcdfe6;
  font-size: 12px;
}

.section-card >>> .el-table .el-input__inner:focus {
  border-color: #409eff;
}

.section-card >>> .el-table .el-select {
  width: 100%;
}

/* 折叠面板样式 */
.section-card >>> .el-collapse {
  border: none;
}

.section-card >>> .el-collapse-item__header {
  background-color: #fafbfc;
  border: 1px solid #e6ebf5;
  border-radius: 4px;
  padding: 0 15px;
  margin-bottom: 10px;
  font-weight: 500;
  color: #333b5b;
}

.section-card >>> .el-collapse-item__header:hover {
  background-color: #f0f2f5;
}

.section-card >>> .el-collapse-item__wrap {
  border: none;
  background-color: #fafbfc;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 10px;
}

.section-card >>> .el-collapse-item__content {
  padding-bottom: 0;
}

/* 对话框底部按钮 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  padding: 10px 0 0 0;
}

/* 响应式优化 */
@media screen and (max-width: 1400px) {
  .monitor-config-dialog >>> .el-dialog {
    width: 95% !important;
  }
}

/* 空数据提示 */
.section-card >>> .el-table__empty-block {
  padding: 30px 0;
}

.section-card >>> .el-table__empty-text {
  color: #909399;
  font-size: 13px;
}

/* Table行配置卡片 */
.table-row-config {
  margin-top: 10px;
}

.table-row-config >>> .el-card__header {
  padding: 10px 15px;
  background-color: #f9fafc;
}

.table-row-config >>> .el-card__body {
  padding: 15px;
}

/* Label项表格样式 */
.table-row-config >>> .el-table {
  margin-top: 5px;
}

.table-row-config >>> .el-table .el-input__inner {
  font-size: 12px;
}

.table-row-config >>> .el-table .el-input-number {
  width: 100%;
}

.table-row-config >>> .el-table .el-input-number .el-input__inner {
  text-align: left;
  padding-left: 10px;
  padding-right: 30px;
}

/* 颜色选择器样式 */
.table-row-config >>> .el-color-picker {
  vertical-align: middle;
}

.table-row-config >>> .el-color-picker__trigger {
  width: 100%;
  height: 32px;
}

/* 表单项间距优化 */
.table-row-config >>> .el-form-item {
  margin-bottom: 10px;
}

.table-row-config >>> .el-form-item__label {
  font-size: 12px;
  color: #606266;
}

/* 操作按钮样式 */
.table-row-config >>> .el-button--text {
  padding: 0;
  margin-right: 8px;
}

.table-row-config >>> .el-button--text:last-child {
  margin-right: 0;
}
</style>
