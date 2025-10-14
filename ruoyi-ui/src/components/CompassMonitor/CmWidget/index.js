import './index.css';

export default {
  props: {
    value: {
      type: Object,
      default: () => ({})
    }
  },
  render() {
    return (
      <div class="cm-widget_wrapper">
        <div class="cm-widget">
          <div class="cm-widget-buttons">
            {this.value.items.map((item, index) => {
              return (
                <el-button
                  key={index}
                  size="small"
                  type={item.type || "primary"}
                  icon={item.icon}
                  onClick={() => {
                    if (item.href && item.href !== '#') {
                      window.open(item.href, item.target || "_blank");
                    }
                  }}
                >
                  {item.text}
                </el-button>
              )
            })}
          </div>
        </div>
      </div>
    );
  }
};