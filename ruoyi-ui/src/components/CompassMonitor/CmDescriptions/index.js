export default {
  props: {
    value: {
      type: Object,
      default: () => ({})
    }
  },
  render(h) {
    return (
      <el-descriptions column={this.value.column} border={this.value.border}>
        {this.value.items.map((item, index) => {
          return (
            <el-descriptions-item>
              <template slot="label">{item.label}</template>
              {item.value}
            </el-descriptions-item>
          );
        })}
      </el-descriptions>
    );
  }
};