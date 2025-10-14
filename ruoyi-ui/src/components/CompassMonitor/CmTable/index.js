import "./index.css";

export default {
  props: {
    value: {
      type: Object,
      default: () => ({})
    }
  },
  render() {
    /* 渲染表头 */
    const renderHeader = () => (
      <tr class={["el-descriptions-row"]}>
        {/* 左上角对角线单元格 */}
        <th
          class={[
            "el-descriptions-item__cell",
            "el-descriptions-item__label",
            "is-bordered-label",
            "diagonal-cell"
          ]}
        >
          <div class="diagonal-line"></div>
        </th>
        {/* 项目名列（colspan=3，包含项目名、子项名、单位） */}
        <th
          class={[
            "el-descriptions-item__cell",
            "el-descriptions-item__label",
            "is-bordered-label",
            "header-project-name"
          ]}
          colSpan={3}
        >
          项目名
        </th>
        {/* 条件列 */}
        <th
          class={[
            "el-descriptions-item__cell",
            "el-descriptions-item__label",
            "is-bordered-label",
            "header-condition"
          ]}
        >
          条件
        </th>
      </tr>
    );

    /* 渲染表体行 */
    const renderBodyRow = (rowData, index, totalRows) => {
      const cells = [];

      // 1. 渲染行头（左侧分类标签）
      if (this.value.body.rowHeader && index === 0) {
        cells.push(
          <th
            class={[
              "el-descriptions-item__cell",
              "el-descriptions-item__label",
              "is-bordered-label",
              "row-header"
            ]}
            rowSpan={totalRows}
          >
            {this.value.body.rowHeader}
          </th>
        );
      }

      // 2. 判断行类型
      const hasUnit = rowData.unit && rowData.unit.trim() !== '';
      const hasSubName = rowData.subName && rowData.subName.trim() !== '';
      const isSubRow = rowData.projectName === ''; // 复杂行的子行（非第一行）

      // 3. 渲染项目名列（第2列）
      if (!isSubRow) {
        // 简单行或复杂行的第一行：渲染项目名
        let projectNameColSpan = 1;
        if (!hasUnit && !hasSubName) {
          // 无单位且无子项：跨3列
          projectNameColSpan = 3;
        } else if (hasUnit && !hasSubName) {
          // 有单位但无子项（简单行）：跨2列
          projectNameColSpan = 2;
        }

        cells.push(
          <th
            class={[
              "el-descriptions-item__cell",
              "el-descriptions-item__label",
              "is-bordered-label",
              "project-name-cell"
            ]}
            colSpan={projectNameColSpan}
            rowSpan={rowData.projectNameRowSpan || 1}
          >
            {rowData.projectName}
          </th>
        );

        // 4. 根据项目名的colSpan，渲染后续列
        if (projectNameColSpan === 1) {
          // 复杂行：渲染子项名和单位
          cells.push(
            <th
              class={[
                "el-descriptions-item__cell",
                "el-descriptions-item__label",
                "is-bordered-label",
                "sub-name-cell"
              ]}
            >
              {rowData.subName}
            </th>
          );
          cells.push(
            <th
              class={[
                "el-descriptions-item__cell",
                "el-descriptions-item__label",
                "is-bordered-label",
                "unit-cell"
              ]}
            >
              {rowData.unit || ''}
            </th>
          );
        } else if (projectNameColSpan === 2) {
          // 简单行有单位：渲染单位
          cells.push(
            <th
              class={[
                "el-descriptions-item__cell",
                "el-descriptions-item__label",
                "is-bordered-label",
                "unit-cell"
              ]}
            >
              {rowData.unit}
            </th>
          );
        }
        // projectNameColSpan === 3 时不需要渲染额外的列
      } else {
        // 复杂行的子行：渲染子项名和单位
        cells.push(
          <th
            class={[
              "el-descriptions-item__cell",
              "el-descriptions-item__label",
              "is-bordered-label",
              "sub-name-cell"
            ]}
          >
            {rowData.subName}
          </th>
        );
        cells.push(
          <th
            class={[
              "el-descriptions-item__cell",
              "el-descriptions-item__label",
              "is-bordered-label",
              "unit-cell"
            ]}
          >
            {rowData.unit || ''}
          </th>
        );
      }

      // 5. 渲染条件值列（第4列）
      cells.push(
        <td
          class={[
            "el-descriptions-item__cell",
            "el-descriptions-item__content",
            "value-cell"
          ]}
        >
          {rowData.value || ''}
        </td>
      );

      return (
        <tr class={["el-descriptions-row"]} key={index}>
          {cells}
        </tr>
      );
    };

    /* 渲染表体 */
    const renderBody = () => {
      if (this.value.body && this.value.body.rows && this.value.body.rows.length > 0) {
        const rows = this.value.body.rows;
        return rows.map((rowData, index) => renderBodyRow(rowData, index, rows.length));
      } else {
        return renderNoData();
      }
    };

    /* 渲染没有数据 */
    const renderNoData = () => (
      <tr>
        <td
          colSpan="4"
          style={{ textAlign: "center", padding: "20px" }}
          class={["el-descriptions-item__cell"]}
        >
          <div style={{ color: "#909399" }}>暂无数据</div>
        </td>
      </tr>
    );

    return (
      <div class={["cm-table"]}>
        <div class={["el-descriptions"]}>
          <div class={["el-descriptions__body"]}>
            <table
              class={["el-descriptions__table", "is-bordered"]}
              style={this.value.style}
            >
              <thead>
                {renderHeader()}
              </thead>
              <tbody>
                {renderBody()}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    );
  }
};