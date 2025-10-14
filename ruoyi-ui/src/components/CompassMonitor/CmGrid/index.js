import CmTable from "../CmTable";
import CmForm from "../CmForm";
import CmDescriptions from "../CmDescriptions";
import "./index.css";
import CmWidget from "../CmWidget";

export default {
  props: {
    value: {
      type: Array,
      default: () => []
    }
  },
  render() {
    const { dialog } = this.$slots || {};
    console.log("dialog", dialog);
    const renderSubNodes = {
      table: value => <CmTable value={value}></CmTable>,
      form: value => <CmForm value={value}></CmForm>,
      descriptions: value => <CmDescriptions value={value}></CmDescriptions>,
      widget: value => <CmWidget value={value}></CmWidget>
    };
    return (
      <div class="cm-grid">
        {this.value.map((row, rIdx) => {
          return (
            <el-row key={rIdx}>
              {row.map((col, cIdx) => {
                return (
                  <el-col span={col.span} offset={col.offset} key={cIdx}>
                    {renderSubNodes[col.type]
                      ? renderSubNodes[col.type](col)
                      : `${col.type}: renderer not found.`}
                  </el-col>
                );
              })}
            </el-row>
          );
        })}
        {dialog}
      </div>
    );
  }
};