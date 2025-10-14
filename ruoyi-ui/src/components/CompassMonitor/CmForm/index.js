export default {
  props: {
    value: {
      type: Object,
      default: () => ({})
    }
  },
  watch: {
    'value.items': {
      handler(val) {
        console.log(JSON.stringify(val));
      },
      deep: true
    }
  },
  data() {
    return {
      formData: {}
    };
  },
  created() {
    /* 初始化表单数据 */
    this.formData = this.value.items.reduce((target, item, index) => {
      target[item.prop] = item.value;
      return target;
    }, {});
  },
  render(h) {
    console.log(JSON.stringify(this.formData));
    /* 渲染表单项 */
    const renderFormatems = value => {
      return value.map((item, index) => {
        return (
          <el-form-item label={item.label} prop={item.prop} key={index}>
            <el-input
              value={this.formData[item.prop]}
              onInput={(val) => {
                console.log('onInput', val);
                this.formData[item.prop] = val;
                // 触发 onChange 事件
                if (this.value.onChange) {
                  this.value.onChange(this.formData);
                }
              }}
            />
          </el-form-item>
        );
      });
    };

    /* 渲染按钮 */
    const renderActions = value => {
      return (
        <el-form-item>
          {this.value.actions.map((item, index) => {
            return (
              <el-button
                type={item.type}
                onClick={() => item.click(this.formData, this.$refs.cmForm, h)}
              >
                {item.text}
              </el-button>
            );
          })}
        </el-form-item>
      );
    };

    return (
      <div class="cm-form">
        <el-form
          ref="cmForm"
          model={this.formData}
          inline={this.value.inline}
          rules={this.value.rules}
          style="text-align: left;"
        >
          {this.value.items && this.value.items.length > 0
            ? renderFormatems(this.value.items)
            : null}
          {this.value.actions && this.value.actions.length > 0
            ? renderActions(this.value.actions)
            : null}
        </el-form>
      </div>
    );
  }
};