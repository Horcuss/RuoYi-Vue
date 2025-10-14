/**
 * 监控配置解析器
 * 将监控配置数据转换为 CompassMonitor 组件所需的格式
 */

/**
 * 解析表单配置
 * @param {Array} formItems - 表单项配置
 * @returns {Object} CmForm 组件所需的配置
 */
export function parseFormConfig(formItems) {
  if (!formItems || formItems.length === 0) {
    return null;
  }

  return {
    span: 16,
    type: "form",
    inline: true,
    items: formItems.map(item => ({
      label: item.label,
      prop: item.prop,
      value: null
    }))
  };
}

/**
 * 解析Widget配置
 * @param {Array} widgetItems - Widget项配置
 * @returns {Object} CmWidget 组件所需的配置
 */
export function parseWidgetConfig(widgetItems) {
  if (!widgetItems || widgetItems.length === 0) {
    return null;
  }

  return {
    span: 8,
    type: "widget",
    items: widgetItems.map(item => ({
      text: item.text,
      href: item.href || '#',
      icon: "el-icon-link",
      target: "_blank",
      type: "primary"
    }))
  };
}

/**
 * 解析Descriptions配置
 * @param {Array} descItems - Descriptions项配置
 * @param {Object} data - 实际数据
 * @returns {Object} CmDescriptions 组件所需的配置
 */
export function parseDescriptionsConfig(descItems, data = {}) {
  if (!descItems || descItems.length === 0) {
    return null;
  }

  return {
    span: 24,
    type: "descriptions",
    column: 4,
    border: true,
    items: descItems.map(item => ({
      label: item.label,
      value: getValueByConfig(item, data)
    }))
  };
}

/**
 * 解析备注配置
 * @param {Array} remarkItems - 备注项配置
 * @param {Object} data - 实际数据
 * @returns {Array} 备注数据数组
 */
export function parseRemarkConfig(remarkItems, data = {}) {
  if (!remarkItems || remarkItems.length === 0) {
    return [];
  }

  return remarkItems.map(item => ({
    title: item.title,
    content: getValueByConfig(item, data)
  }));
}

/**
 * 解析Table配置
 * @param {Array} tableConfigs - Table配置数组
 * @param {Object} data - 实际数据
 * @returns {Array} CmTable 组件所需的配置数组
 */
export function parseTableConfig(tableConfigs, data = {}) {
  if (!tableConfigs || tableConfigs.length === 0) {
    return [];
  }

  return tableConfigs.map(tableConfig => ({
    span: 24,
    type: "table",
    header: [
      { label: "项目名", style: { width: "45%" } },
      { label: "条件", style: { width: "40%" } }
    ],
    body: {
      rowHeader: tableConfig.rowHeader,
      rows: parseTableRows(tableConfig.rows, data)
    }
  }));
}

/**
 * 解析Table行配置
 * @param {Array} rows - 行配置数组
 * @param {Object} data - 实际数据
 * @returns {Array} 行数据数组
 */
function parseTableRows(rows, data) {
  if (!rows || rows.length === 0) {
    return [];
  }

  return rows.map(row => {
    const value = getValueByConfig(row, data);

    // 解析label结构
    const label = parseLabelStructure(row.labelItems);

    // 构建行数据
    const rowData = {
      label: label,
      value: value
    };

    // 添加value样式（如果配置了）
    if (row.valueStyle && hasValidStyle(row.valueStyle)) {
      rowData.contentStyle = buildStyleObject(row.valueStyle);
    }

    return rowData;
  });
}

/**
 * 解析Label结构
 * @param {Array} labelItems - Label项数组
 * @returns {Array|String} 解析后的label结构
 */
function parseLabelStructure(labelItems) {
  if (!labelItems || labelItems.length === 0) {
    return '';
  }

  // 如果只有一个简单的label项，直接返回字符串
  if (labelItems.length === 1 &&
      !labelItems[0].colSpan &&
      !labelItems[0].rowSpan &&
      labelItems[0].backgroundColor === 'default') {
    return labelItems[0].content || '';
  }

  // 否则返回复杂的label数组结构
  return labelItems.map(item => {
    // 如果是简单的字符串项（没有任何特殊属性）
    if (!item.colSpan && !item.rowSpan && item.backgroundColor === 'default') {
      return item.content || '';
    }

    // 构建复杂的label对象
    const labelObj = {
      content: item.content || ''
    };

    // 添加colSpan
    if (item.colSpan && item.colSpan > 1) {
      labelObj.colSpan = item.colSpan;
    }

    // 添加rowSpan
    if (item.rowSpan && item.rowSpan > 1) {
      labelObj.rowSpan = item.rowSpan;
    }

    // 添加样式
    const style = {};
    if (item.backgroundColor && item.backgroundColor !== 'default') {
      if (item.backgroundColor === 'custom' && item.customColor) {
        style.backgroundColor = item.customColor;
      } else {
        style.backgroundColor = item.backgroundColor;
      }
    }

    if (Object.keys(style).length > 0) {
      labelObj.style = style;
    }

    return labelObj;
  });
}

/**
 * 检查样式对象是否有有效值
 * @param {Object} styleObj - 样式对象
 * @returns {Boolean} 是否有有效样式
 */
function hasValidStyle(styleObj) {
  if (!styleObj) return false;
  return !!(styleObj.color || styleObj.backgroundColor || styleObj.fontSize);
}

/**
 * 构建样式对象
 * @param {Object} styleConfig - 样式配置
 * @returns {Object} 样式对象
 */
function buildStyleObject(styleConfig) {
  const style = {};

  if (styleConfig.color) {
    style.color = styleConfig.color;
  }

  if (styleConfig.backgroundColor) {
    style.backgroundColor = styleConfig.backgroundColor;
  }

  if (styleConfig.fontSize) {
    style.fontSize = styleConfig.fontSize;
  }

  return style;
}

/**
 * 根据配置获取值
 * @param {Object} config - 配置对象（包含dataSource、displayType、expression）
 * @param {Object} data - 数据对象
 * @returns {*} 计算后的值
 */
function getValueByConfig(config, data) {
  if (!config.expression) {
    return 'N/A';
  }

  try {
    if (config.displayType === 'computed') {
      // 运算后显示 - 执行表达式
      // 注意：这里使用 Function 构造函数执行表达式，实际生产环境需要更安全的方式
      const func = new Function('data', 'value', `return ${config.expression}`);
      const value = getNestedValue(data, config.expression);
      return func(data, value);
    } else {
      // 直接显示 - 从数据中获取字段值
      return getNestedValue(data, config.expression) || 'N/A';
    }
  } catch (error) {
    console.error('解析配置值失败:', error);
    return 'Error';
  }
}

/**
 * 获取嵌套对象的值
 * @param {Object} obj - 对象
 * @param {String} path - 路径（支持点号分隔，如：user.name）
 * @returns {*} 值
 */
function getNestedValue(obj, path) {
  if (!obj || !path) {
    return undefined;
  }

  const keys = path.split('.');
  let result = obj;

  for (const key of keys) {
    if (result && typeof result === 'object' && key in result) {
      result = result[key];
    } else {
      return undefined;
    }
  }

  return result;
}

/**
 * 解析完整的监控配置
 * @param {Object} config - 监控配置对象
 * @param {Object} data - 实际数据
 * @returns {Object} 解析后的完整配置
 */
export function parseMonitorConfig(config, data = {}) {
  if (!config) {
    return null;
  }

  const result = {
    headerData: [],
    infoData: [],
    remarks: [],
    tables: []
  };

  // 解析顶部区域（Form + Widget）
  const formConfig = parseFormConfig(config.formItems);
  const widgetConfig = parseWidgetConfig(config.widgetItems);
  
  if (formConfig || widgetConfig) {
    const headerRow = [];
    if (formConfig) headerRow.push(formConfig);
    if (widgetConfig) headerRow.push(widgetConfig);
    result.headerData.push(headerRow);
  }

  // 解析基础信息区域
  const descConfig = parseDescriptionsConfig(config.descItems, data);
  if (descConfig) {
    result.infoData.push([descConfig]);
  }

  // 解析备注区域
  result.remarks = parseRemarkConfig(config.remarkItems, data);

  // 解析表格区域
  result.tables = parseTableConfig(config.tableConfigs, data);

  return result;
}

/**
 * 验证监控配置
 * @param {Object} config - 监控配置对象
 * @returns {Object} 验证结果 { valid: boolean, errors: Array }
 */
export function validateMonitorConfig(config) {
  const errors = [];

  if (!config) {
    errors.push('配置对象不能为空');
    return { valid: false, errors };
  }

  if (!config.code) {
    errors.push('监控页面KEY不能为空');
  }

  if (!config.name) {
    errors.push('监控页面名称不能为空');
  }

  // 验证表单项
  if (config.formItems && config.formItems.length > 0) {
    config.formItems.forEach((item, index) => {
      if (!item.label) {
        errors.push(`表单项${index + 1}的中文标签不能为空`);
      }
      if (!item.prop) {
        errors.push(`表单项${index + 1}的英文字段不能为空`);
      }
    });
  }

  // 验证Widget项
  if (config.widgetItems && config.widgetItems.length > 0) {
    config.widgetItems.forEach((item, index) => {
      if (!item.text) {
        errors.push(`链接按钮${index + 1}的按钮文字不能为空`);
      }
    });
  }

  // 验证Descriptions项
  if (config.descItems && config.descItems.length > 0) {
    config.descItems.forEach((item, index) => {
      if (!item.label) {
        errors.push(`基础信息项${index + 1}的中文标签不能为空`);
      }
      if (!item.expression) {
        errors.push(`基础信息项${index + 1}的表达式/字段不能为空`);
      }
    });
  }

  return {
    valid: errors.length === 0,
    errors
  };
}

export default {
  parseFormConfig,
  parseWidgetConfig,
  parseDescriptionsConfig,
  parseRemarkConfig,
  parseTableConfig,
  parseMonitorConfig,
  validateMonitorConfig
};

