const fs = require('fs');
const { Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell, 
        Header, Footer, AlignmentType, HeadingLevel, BorderStyle, WidthType, 
        PageNumber, LevelFormat, PageBreak, ShadingType } = require('docx');

// 创建文档
const doc = new Document({
  styles: {
    default: {
      document: {
        run: { font: "宋体", size: 24 } // 小四宋体
      }
    },
    paragraphStyles: [
      {
        id: "Title",
        name: "Title",
        basedOn: "Normal",
        run: { size: 44, bold: true, font: "黑体" },
        paragraph: { spacing: { before: 240, after: 120 }, alignment: AlignmentType.CENTER }
      },
      {
        id: "Heading1",
        name: "Heading 1",
        basedOn: "Normal",
        next: "Normal",
        quickFormat: true,
        run: { size: 32, bold: true, font: "黑体" },
        paragraph: { spacing: { before: 240, after: 240 }, outlineLevel: 0 }
      },
      {
        id: "Heading2",
        name: "Heading 2",
        basedOn: "Normal",
        next: "Normal",
        quickFormat: true,
        run: { size: 28, bold: true, font: "黑体" },
        paragraph: { spacing: { before: 180, after: 180 }, outlineLevel: 1 }
      }
    ]
  },
  sections: [
    {
      properties: {
        page: {
          margin: { top: 1440, right: 1440, bottom: 1440, left: 1440 }
        }
      },
      children: [
        // ========== 封面部分 ==========
        // 学院名称
        new Paragraph({
          alignment: AlignmentType.CENTER,
          spacing: { before: 600, after: 200 },
          children: [
            new TextRun({ text: "成都文理学院·人工智能与大数据学院", font: "黑体", size: 44, bold: true })
          ]
        }),
        
        // 作品报告标题
        new Paragraph({
          alignment: AlignmentType.CENTER,
          spacing: { before: 400, after: 400 },
          children: [
            new TextRun({ text: "作品报告", font: "黑体", size: 56, bold: true })
          ]
        }),
        
        // 科目
        new Paragraph({
          alignment: AlignmentType.CENTER,
          spacing: { before: 200, after: 200 },
          children: [
            new TextRun({ text: "科目：移动应用开发技术", font: "宋体", size: 28, bold: true })
          ]
        }),
        
        // 信息表格
        new Table({
          columnWidths: [2000, 1000, 6360],
          rows: [
            createInfoRow("项目名称", "：", "MyAndroid 密码管理器"),
            createInfoRow("学期", "：", "2024－2025学年第二学期"),
            createInfoRow("班级", "：", "计科3班"),
            createInfoRow("学号", "：", "233817310311"),
            createInfoRow("姓名", "：", "程瑞琪"),
            createInfoRow("完成时间", "：", "2026年6月 8 日")
          ]
        }),
        
        // 分页
        new Paragraph({ children: [new PageBreak()] }),
        
        // ========== 目录页 ==========
        new Paragraph({
          alignment: AlignmentType.CENTER,
          spacing: { before: 400, after: 400 },
          children: [
            new TextRun({ text: "目 录", font: "黑体", size: 36, bold: true })
          ]
        }),
        
        // 目录内容
        new Paragraph({
          spacing: { before: 200, after: 100 },
          children: [new TextRun({ text: "1 课题设计内容", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 100, after: 100 },
          indent: { left: 480 },
          children: [new TextRun({ text: "1.1 课程设计基本功能描述", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 100, after: 100 },
          indent: { left: 480 },
          children: [new TextRun({ text: "1.2 流程图", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 100, after: 100 },
          indent: { left: 480 },
          children: [new TextRun({ text: "1.3 数据库设计", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 100, after: 100 },
          indent: { left: 480 },
          children: [new TextRun({ text: "1.4 程序运行结果", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 200, after: 100 },
          children: [new TextRun({ text: "2 课程设计总结", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 100, after: 100 },
          indent: { left: 480 },
          children: [new TextRun({ text: "2.1 课程设计完成情况说明", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 100, after: 100 },
          indent: { left: 480 },
          children: [new TextRun({ text: "2.2 课程设计心得", font: "宋体", size: 24 })]
        }),
        
        // 分页
        new Paragraph({ children: [new PageBreak()] }),
        
        // ========== 第一章：课题设计内容 ==========
        new Paragraph({
          heading: HeadingLevel.HEADING_1,
          spacing: { before: 300, after: 200 },
          children: [new TextRun({ text: "1 课题设计内容", font: "黑体", size: 32, bold: true })]
        }),
        
        // 1.1 课程设计基本功能描述
        new Paragraph({
          heading: HeadingLevel.HEADING_2,
          spacing: { before: 200, after: 150 },
          children: [new TextRun({ text: "1.1 课程设计基本功能描述", font: "黑体", size: 28, bold: true })]
        }),
        
        // 提示信息（红色）
        new Paragraph({
          spacing: { before: 100, after: 150 },
          children: [new TextRun({ 
            text: "正文1.5倍行距，小四宋体（源码字体可以适当修改），报告完成后，请更新目录页码（注意不要修改目录格式，单击更新目录，只更新页码，阅读后请删除红色部分）", 
            font: "宋体", 
            size: 24,
            color: "FF0000"
          })]
        }),
        
        // 项目概述
        new Paragraph({
          spacing: { before: 150, after: 100 },
          children: [new TextRun({ text: "项目名称：MyAndroid（密码管理器）", font: "宋体", size: 24, bold: true })]
        }),
        new Paragraph({
          spacing: { before: 100, after: 150 },
          indent: { firstLine: 480 },
          children: [new TextRun({ text: "MyAndroid 是一款基于 Android 平台的本地密码管理应用，旨在帮助用户安全地存储和管理各类账号密码、API Key 等敏感信息。应用采用现代化的 Android 开发技术栈，遵循 MVVM 架构模式，实现了密码的加密存储、分类管理、搜索查找、数据导出导入以及 WebDAV 云端同步等功能。", font: "宋体", size: 24 })]
        }),
        
        // 开发目标
        new Paragraph({
          spacing: { before: 150, after: 100 },
          children: [new TextRun({ text: "开发目标：", font: "宋体", size: 24, bold: true })]
        }),
        new Paragraph({
          spacing: { before: 50, after: 50 },
          indent: { left: 720 },
          children: [new TextRun({ text: "• 提供安全可靠的密码存储方案", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 50, after: 50 },
          indent: { left: 720 },
          children: [new TextRun({ text: "• 实现直观易用的用户界面", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 50, after: 50 },
          indent: { left: 720 },
          children: [new TextRun({ text: "• 支持数据备份与恢复", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 50, after: 100 },
          indent: { left: 720 },
          children: [new TextRun({ text: "• 支持多设备数据同步", font: "宋体", size: 24 })]
        }),
        
        // 技术架构
        new Paragraph({
          spacing: { before: 200, after: 100 },
          children: [new TextRun({ text: "技术架构：", font: "宋体", size: 24, bold: true })]
        }),
        
        // 技术栈表格
        new Table({
          columnWidths: [2500, 3500, 3360],
          rows: [
            createTableRow(["技术领域", "技术选型", "说明"], true),
            createTableRow(["编程语言", "Java", "主要开发语言"]),
            createTableRow(["UI 框架", "XML + ViewBinding", "传统 Android UI 开发方式"]),
            createTableRow(["架构模式", "MVVM", "Model-View-ViewModel"]),
            createTableRow(["数据库", "Room", "Android Jetpack 持久化库"]),
            createTableRow(["异步处理", "LiveData + ExecutorService", "响应式数据 + 线程池"]),
            createTableRow(["网络请求", "OkHttp", "HTTP 客户端"]),
            createTableRow(["数据序列化", "Gson", "JSON 解析库"]),
            createTableRow(["加密算法", "AES-256-GCM + PBKDF2", "企业级加密标准"]),
            createTableRow(["生物识别", "Biometric API", "指纹/面部识别"])
          ]
        }),
        
        // MVVM架构说明
        new Paragraph({
          spacing: { before: 250, after: 100 },
          children: [new TextRun({ text: "MVVM 架构说明：", font: "宋体", size: 24, bold: true })]
        }),
        new Paragraph({
          spacing: { before: 100, after: 100 },
          indent: { firstLine: 480 },
          children: [new TextRun({ text: "本项目采用 MVVM（Model-View-ViewModel）架构模式，将应用分为三个层次：", font: "宋体", size: 24 })]
        }),
        
        // View层
        new Paragraph({
          spacing: { before: 100, after: 50 },
          indent: { left: 720 },
          children: [new TextRun({ text: "View 层（Activity / XML Layout）：", font: "宋体", size: 24, bold: true })]
        }),
        new Paragraph({
          spacing: { before: 30, after: 30 },
          indent: { left: 1080 },
          children: [new TextRun({ text: "• MainActivity - 主页，显示密码列表", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 30, after: 30 },
          indent: { left: 1080 },
          children: [new TextRun({ text: "• AddEditActivity - 添加/编辑密码", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 30, after: 30 },
          indent: { left: 1080 },
          children: [new TextRun({ text: "• CategoryActivity - 分类管理", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 30, after: 30 },
          indent: { left: 1080 },
          children: [new TextRun({ text: "• SearchActivity - 搜索功能", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 30, after: 50 },
          indent: { left: 1080 },
          children: [new TextRun({ text: "• SettingsActivity - 设置页面", font: "宋体", size: 24 })]
        }),
        
        // ViewModel层
        new Paragraph({
          spacing: { before: 100, after: 50 },
          indent: { left: 720 },
          children: [new TextRun({ text: "ViewModel 层：", font: "宋体", size: 24, bold: true })]
        }),
        new Paragraph({
          spacing: { before: 30, after: 30 },
          indent: { left: 1080 },
          children: [new TextRun({ text: "• MainViewModel - 主页业务逻辑", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 30, after: 30 },
          indent: { left: 1080 },
          children: [new TextRun({ text: "• AddEditViewModel - 添加编辑业务逻辑", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 30, after: 30 },
          indent: { left: 1080 },
          children: [new TextRun({ text: "• CategoryViewModel - 分类管理业务逻辑", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 30, after: 30 },
          indent: { left: 1080 },
          children: [new TextRun({ text: "• SearchViewModel - 搜索业务逻辑", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 30, after: 50 },
          indent: { left: 1080 },
          children: [new TextRun({ text: "• SettingsViewModel - 设置业务逻辑", font: "宋体", size: 24 })]
        }),
        
        // Model层
        new Paragraph({
          spacing: { before: 100, after: 50 },
          indent: { left: 720 },
          children: [new TextRun({ text: "Model 层：", font: "宋体", size: 24, bold: true })]
        }),
        new Paragraph({
          spacing: { before: 30, after: 30 },
          indent: { left: 1080 },
          children: [new TextRun({ text: "• Repository：PasswordRepository（密码数据仓库）、CategoryRepository（分类数据仓库）", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 30, after: 30 },
          indent: { left: 1080 },
          children: [new TextRun({ text: "• DAO：PasswordDao（密码数据访问对象）、CategoryDao（分类数据访问对象）", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 30, after: 100 },
          indent: { left: 1080 },
          children: [new TextRun({ text: "• Entity：PasswordEntry（密码实体类）、Category（分类实体类）", font: "宋体", size: 24 })]
        }),
        
        // 核心功能模块
        new Paragraph({
          spacing: { before: 200, after: 100 },
          children: [new TextRun({ text: "核心功能模块：", font: "宋体", size: 24, bold: true })]
        }),
        
        // 1. 密码记录模块
        new Paragraph({
          spacing: { before: 150, after: 80 },
          indent: { left: 480 },
          children: [new TextRun({ text: "1. 密码记录模块", font: "宋体", size: 24, bold: true })]
        }),
        new Paragraph({
          spacing: { before: 40, after: 40 },
          indent: { left: 840 },
          children: [new TextRun({ text: "• 增删改查：完整管理密码条目的生命周期", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 40, after: 40 },
          indent: { left: 840 },
          children: [new TextRun({ text: "• 密码显示/隐藏：保护敏感信息不被旁人窥视", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 40, after: 40 },
          indent: { left: 840 },
          children: [new TextRun({ text: "• 一键复制：快速复制用户名、密码到剪贴板", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 40, after: 40 },
          indent: { left: 840 },
          children: [new TextRun({ text: "• 收藏功能：标记常用密码，快速访问", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 40, after: 80 },
          indent: { left: 840 },
          children: [new TextRun({ text: "• 密码类型：支持普通密码和 API Key 两种类型", font: "宋体", size: 24 })]
        }),
        
        // 数据字段
        new Paragraph({
          spacing: { before: 80, after: 40 },
          indent: { left: 840 },
          children: [new TextRun({ text: "数据字段：", font: "宋体", size: 24, bold: true })]
        }),
        new Paragraph({
          spacing: { before: 30, after: 30 },
          indent: { left: 1200 },
          children: [new TextRun({ text: "• id - 主键，自增", font: "宋体", size: 22 })]
        }),
        new Paragraph({
          spacing: { before: 30, after: 30 },
          indent: { left: 1200 },
          children: [new TextRun({ text: "• categoryId - 外键，关联分类", font: "宋体", size: 22 })]
        }),
        new Paragraph({
          spacing: { before: 30, after: 30 },
          indent: { left: 1200 },
          children: [new TextRun({ text: "• title - 标题（如：淘宝、GitHub）", font: "宋体", size: 22 })]
        }),
        new Paragraph({
          spacing: { before: 30, after: 30 },
          indent: { left: 1200 },
          children: [new TextRun({ text: "• username - 用户名", font: "宋体", size: 22 })]
        }),
        new Paragraph({
          spacing: { before: 30, after: 30 },
          indent: { left: 1200 },
          children: [new TextRun({ text: "• password - 密码（AES加密存储）", font: "宋体", size: 22 })]
        }),
        new Paragraph({
          spacing: { before: 30, after: 30 },
          indent: { left: 1200 },
          children: [new TextRun({ text: "• url - 网址", font: "宋体", size: 22 })]
        }),
        new Paragraph({
          spacing: { before: 30, after: 30 },
          indent: { left: 1200 },
          children: [new TextRun({ text: "• notes - 备注", font: "宋体", size: 22 })]
        }),
        new Paragraph({
          spacing: { before: 30, after: 30 },
          indent: { left: 1200 },
          children: [new TextRun({ text: "• entryType - 类型：0=密码，1=API Key", font: "宋体", size: 22 })]
        }),
        new Paragraph({
          spacing: { before: 30, after: 30 },
          indent: { left: 1200 },
          children: [new TextRun({ text: "• isFavorite - 是否收藏", font: "宋体", size: 22 })]
        }),
        new Paragraph({
          spacing: { before: 30, after: 30 },
          indent: { left: 1200 },
          children: [new TextRun({ text: "• createdAt - 创建时间", font: "宋体", size: 22 })]
        }),
        new Paragraph({
          spacing: { before: 30, after: 100 },
          indent: { left: 1200 },
          children: [new TextRun({ text: "• updatedAt - 更新时间", font: "宋体", size: 22 })]
        }),
        
        // 2. 分类管理模块
        new Paragraph({
          spacing: { before: 150, after: 80 },
          indent: { left: 480 },
          children: [new TextRun({ text: "2. 分类管理模块", font: "宋体", size: 24, bold: true })]
        }),
        new Paragraph({
          spacing: { before: 40, after: 40 },
          indent: { left: 840 },
          children: [new TextRun({ text: "• 自定义分类：用户可创建、编辑、删除分类", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 40, after: 40 },
          indent: { left: 840 },
          children: [new TextRun({ text: "• 分类图标：每个分类可设置专属图标", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 40, after: 40 },
          indent: { left: 840 },
          children: [new TextRun({ text: "• 排序功能：支持自定义分类排序顺序", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 40, after: 80 },
          indent: { left: 840 },
          children: [new TextRun({ text: "• 按分类筛选：主页可按分类过滤密码列表", font: "宋体", size: 24 })]
        }),
        
        // 预置分类表格
        new Paragraph({
          spacing: { before: 80, after: 40 },
          indent: { left: 840 },
          children: [new TextRun({ text: "预置分类：", font: "宋体", size: 24, bold: true })]
        }),
        new Table({
          columnWidths: [3120, 3120, 3120],
          rows: [
            createTableRow(["分类名称", "图标标识", "排序"], true),
            createTableRow(["社交", "ic_social", "0"]),
            createTableRow(["银行", "ic_bank", "1"]),
            createTableRow(["邮箱", "ic_email", "2"]),
            createTableRow(["工作", "ic_work", "3"]),
            createTableRow(["API Key", "ic_api", "4"]),
            createTableRow(["其他", "ic_other", "5"])
          ]
        }),
        
        // 3. 搜索模块
        new Paragraph({
          spacing: { before: 150, after: 80 },
          indent: { left: 480 },
          children: [new TextRun({ text: "3. 搜索模块", font: "宋体", size: 24, bold: true })]
        }),
        new Paragraph({
          spacing: { before: 40, after: 40 },
          indent: { left: 840 },
          children: [new TextRun({ text: "• 模糊搜索：按标题、用户名进行模糊匹配", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 40, after: 40 },
          indent: { left: 840 },
          children: [new TextRun({ text: "• 实时搜索：输入即时显示搜索结果", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 40, after: 100 },
          indent: { left: 840 },
          children: [new TextRun({ text: "• 搜索结果高亮：方便用户快速定位", font: "宋体", size: 24 })]
        }),
        
        // 4. 数据同步模块
        new Paragraph({
          spacing: { before: 150, after: 80 },
          indent: { left: 480 },
          children: [new TextRun({ text: "4. 数据同步模块", font: "宋体", size: 24, bold: true })]
        }),
        new Paragraph({
          spacing: { before: 40, after: 40 },
          indent: { left: 840 },
          children: [new TextRun({ text: "• 导出功能：将所有密码数据导出为加密的 JSON 文件，使用用户设置的主密码进行 AES-256-GCM 加密", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 40, after: 40 },
          indent: { left: 840 },
          children: [new TextRun({ text: "• 导入功能：从加密的 JSON 文件恢复密码数据，验证主密码后解密并导入，支持覆盖或合并导入策略", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 40, after: 100 },
          indent: { left: 840 },
          children: [new TextRun({ text: "• WebDAV 同步：支持配置 WebDAV 服务器地址，支持上传本地数据到云端，支持从云端下载数据到本地，自动处理冲突合并", font: "宋体", size: 24 })]
        }),
        
        // 1.2 流程图
        new Paragraph({
          heading: HeadingLevel.HEADING_2,
          spacing: { before: 200, after: 150 },
          children: [new TextRun({ text: "1.2 流程图", font: "黑体", size: 28, bold: true })]
        }),
        new Paragraph({
          spacing: { before: 100, after: 100 },
          indent: { firstLine: 480 },
          children: [new TextRun({ text: "采用流程图、NS图、伪代码的其中一种形式，将作品的运行过程描述出来。", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 100, after: 100 },
          indent: { firstLine: 480 },
          children: [new TextRun({ text: "（此处可插入应用流程图，展示用户从登录到管理密码的主要操作流程）", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 100, after: 150 },
          indent: { firstLine: 480 },
          children: [new TextRun({ text: "主要流程包括：用户启动应用 → 生物识别/主密码验证 → 进入主页 → 查看/搜索/管理密码 → 添加/编辑密码 → 分类管理 → 数据导出/导入 → WebDAV同步", font: "宋体", size: 24 })]
        }),
        
        // 1.3 数据库设计
        new Paragraph({
          heading: HeadingLevel.HEADING_2,
          spacing: { before: 200, after: 150 },
          children: [new TextRun({ text: "1.3 数据库设计", font: "黑体", size: 28, bold: true })]
        }),
        new Paragraph({
          spacing: { before: 100, after: 100 },
          children: [new TextRun({ text: "数据库表结构设计：", font: "宋体", size: 24, bold: true })]
        }),
        
        // password_entries 表
        new Paragraph({
          spacing: { before: 150, after: 80 },
          children: [new TextRun({ text: "1. password_entries 表（密码表）", font: "宋体", size: 24, bold: true })]
        }),
        new Table({
          columnWidths: [2000, 1500, 3000, 2860],
          rows: [
            createTableRow(["字段名", "类型", "约束", "说明"], true),
            createTableRow(["id", "INTEGER", "PRIMARY KEY AUTOINCREMENT", "主键"]),
            createTableRow(["category_id", "INTEGER", "FOREIGN KEY, INDEX", "分类外键"]),
            createTableRow(["title", "TEXT", "NOT NULL", "标题"]),
            createTableRow(["username", "TEXT", "", "用户名"]),
            createTableRow(["password", "TEXT", "NOT NULL", "密码（加密存储）"]),
            createTableRow(["url", "TEXT", "", "网址"]),
            createTableRow(["notes", "TEXT", "", "备注"]),
            createTableRow(["entry_type", "INTEGER", "NOT NULL DEFAULT 0", "类型：0=密码，1=API Key"]),
            createTableRow(["is_favorite", "INTEGER", "NOT NULL DEFAULT 0, INDEX", "是否收藏"]),
            createTableRow(["created_at", "INTEGER", "NOT NULL", "创建时间戳"]),
            createTableRow(["updated_at", "INTEGER", "NOT NULL, INDEX", "更新时间戳"])
          ]
        }),
        
        // categories 表
        new Paragraph({
          spacing: { before: 200, after: 80 },
          children: [new TextRun({ text: "2. categories 表（分类表）", font: "宋体", size: 24, bold: true })]
        }),
        new Table({
          columnWidths: [2000, 1500, 3000, 2860],
          rows: [
            createTableRow(["字段名", "类型", "约束", "说明"], true),
            createTableRow(["id", "INTEGER", "PRIMARY KEY AUTOINCREMENT", "主键"]),
            createTableRow(["name", "TEXT", "NOT NULL", "分类名称"]),
            createTableRow(["icon", "TEXT", "", "图标标识"]),
            createTableRow(["sort_order", "INTEGER", "NOT NULL DEFAULT 0", "排序顺序"]),
            createTableRow(["created_at", "INTEGER", "NOT NULL", "创建时间戳"])
          ]
        }),
        
        // 表关系
        new Paragraph({
          spacing: { before: 150, after: 80 },
          children: [new TextRun({ text: "表关系：", font: "宋体", size: 24, bold: true })]
        }),
        new Paragraph({
          spacing: { before: 80, after: 80 },
          indent: { firstLine: 480 },
          children: [new TextRun({ text: "categories 表与 password_entries 表为一对多关系，通过 category_id 外键关联。当删除分类时，将该分类下的密码条目的 category_id 设置为 NULL。", font: "宋体", size: 24 })]
        }),
        
        // 数据库迁移
        new Paragraph({
          spacing: { before: 150, after: 80 },
          children: [new TextRun({ text: "数据库迁移：", font: "宋体", size: 24, bold: true })]
        }),
        new Paragraph({
          spacing: { before: 80, after: 80 },
          indent: { firstLine: 480 },
          children: [new TextRun({ text: "版本 1 → 2 迁移：新增 entry_type 和 is_favorite 字段，并为 is_favorite 创建索引。", font: "宋体", size: 24 })]
        }),
        
        // 1.4 程序运行结果
        new Paragraph({
          heading: HeadingLevel.HEADING_2,
          spacing: { before: 200, after: 150 },
          children: [new TextRun({ text: "1.4 程序运行结果", font: "黑体", size: 28, bold: true })]
        }),
        new Paragraph({
          spacing: { before: 100, after: 100 },
          indent: { firstLine: 480 },
          children: [new TextRun({ text: "运行效果截图介绍。", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 100, after: 100 },
          indent: { firstLine: 480 },
          children: [new TextRun({ text: "（此处可插入应用运行截图，展示主要功能界面，包括：）", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 40, after: 40 },
          indent: { left: 720 },
          children: [new TextRun({ text: "• 主页面：密码列表展示、分类筛选、搜索功能", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 40, after: 40 },
          indent: { left: 720 },
          children: [new TextRun({ text: "• 添加/编辑页面：表单输入、密码强度检测、分类选择", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 40, after: 40 },
          indent: { left: 720 },
          children: [new TextRun({ text: "• 分类管理页面：分类列表、添加/编辑/删除分类", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 40, after: 40 },
          indent: { left: 720 },
          children: [new TextRun({ text: "• 设置页面：生物识别设置、WebDAV配置、数据导出导入", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 40, after: 100 },
          indent: { left: 720 },
          children: [new TextRun({ text: "• 搜索页面：实时搜索结果展示", font: "宋体", size: 24 })]
        }),
        
        // 分页
        new Paragraph({ children: [new PageBreak()] }),
        
        // ========== 第二章：课程设计总结 ==========
        new Paragraph({
          heading: HeadingLevel.HEADING_1,
          spacing: { before: 300, after: 200 },
          children: [new TextRun({ text: "2 课程设计总结", font: "黑体", size: 32, bold: true })]
        }),
        
        // 2.1 课程设计完成情况说明
        new Paragraph({
          heading: HeadingLevel.HEADING_2,
          spacing: { before: 200, after: 150 },
          children: [new TextRun({ text: "2.1 课程设计完成情况说明", font: "黑体", size: 28, bold: true })]
        }),
        new Paragraph({
          spacing: { before: 100, after: 100 },
          indent: { firstLine: 480 },
          children: [new TextRun({ text: "针对课程设计完成的情况进行说明，完成了哪些功能，哪些功能未完成，已完成的功能程序是否能正常运行。", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 150, after: 100 },
          children: [new TextRun({ text: "开发阶段完成情况：", font: "宋体", size: 24, bold: true })]
        }),
        
        // 开发阶段表格
        new Table({
          columnWidths: [1500, 5360, 2500],
          rows: [
            createTableRow(["阶段", "内容", "状态"], true),
            createTableRow(["阶段 1", "基础架构搭建（依赖、Entity、DAO、Database、Repository）", "✅ 完成"]),
            createTableRow(["阶段 2", "主页 + 密码列表（布局、ViewModel、Adapter、筛选）", "✅ 完成"]),
            createTableRow(["阶段 3", "添加/编辑功能（表单、ViewModel、Activity）", "✅ 完成"]),
            createTableRow(["阶段 4", "分类管理（CRUD、弹窗、数量统计）", "✅ 完成"]),
            createTableRow(["阶段 5", "搜索功能（模糊搜索、实时结果）", "✅ 完成"]),
            createTableRow(["阶段 6", "数据加密 + 导出导入（CryptoUtils、文件操作）", "✅ 完成"]),
            createTableRow(["阶段 7", "WebDAV 同步（配置、上传、下载）", "✅ 完成"]),
            createTableRow(["阶段 8", "优化完善（Bug修复、代码重构、主题统一）", "✅ 完成"])
          ]
        }),
        
        // 项目亮点
        new Paragraph({
          spacing: { before: 200, after: 100 },
          children: [new TextRun({ text: "项目亮点：", font: "宋体", size: 24, bold: true })]
        }),
        new Paragraph({
          spacing: { before: 40, after: 40 },
          indent: { left: 480 },
          children: [new TextRun({ text: "1. 安全性高：采用 AES-256-GCM 加密，PBKDF2 密钥派生，10万次迭代", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 40, after: 40 },
          indent: { left: 480 },
          children: [new TextRun({ text: "2. 架构清晰：严格遵循 MVVM 架构，代码职责分明", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 40, after: 40 },
          indent: { left: 480 },
          children: [new TextRun({ text: "3. 数据响应式：使用 LiveData 实现数据自动更新 UI", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 40, after: 40 },
          indent: { left: 480 },
          children: [new TextRun({ text: "4. 异步处理：使用线程池处理数据库操作，避免主线程阻塞", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 40, after: 40 },
          indent: { left: 480 },
          children: [new TextRun({ text: "5. 用户体验：支持生物识别、一键复制、密码强度检测", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 40, after: 40 },
          indent: { left: 480 },
          children: [new TextRun({ text: "6. 数据可靠：支持导出导入、WebDAV 多设备同步", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 40, after: 100 },
          indent: { left: 480 },
          children: [new TextRun({ text: "7. 代码规范：完整的注释、清晰的命名、合理的包结构", font: "宋体", size: 24 })]
        }),
        
        // 2.2 课程设计心得
        new Paragraph({
          heading: HeadingLevel.HEADING_2,
          spacing: { before: 200, after: 150 },
          children: [new TextRun({ text: "2.2 课程设计心得", font: "黑体", size: 28, bold: true })]
        }),
        new Paragraph({
          spacing: { before: 100, after: 100 },
          indent: { firstLine: 480 },
          children: [new TextRun({ text: "针对课程设计过程中，出现的问题，解决方案，自学内容等，描述课程设计心得，不得少于300字", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 100, after: 100 },
          indent: { firstLine: 480 },
          children: [new TextRun({ text: "通过本次课程设计，我全面实践了 Android 应用开发的核心技术，包括 MVVM 架构的设计与实现、Room 数据库的使用与迁移、LiveData 响应式编程、加密算法的应用、网络请求与数据同步以及 Material Design UI 组件的使用。", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 100, after: 100 },
          indent: { firstLine: 480 },
          children: [new TextRun({ text: "在开发过程中，我遇到了许多挑战，如数据库迁移、加密算法的实现、WebDAV 同步等，但通过查阅文档、搜索资料和反复调试，最终都成功解决了。这个项目让我深刻理解了 Android 应用开发的完整流程，提高了我的编程能力和解决问题的能力。", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 100, after: 100 },
          indent: { firstLine: 480 },
          children: [new TextRun({ text: "在技术方面，我学会了如何使用 Room 数据库进行数据持久化，如何使用 LiveData 实现数据与 UI 的自动同步，如何使用 AES-256-GCM 加密算法保护敏感数据，以及如何使用 OkHttp 进行网络请求实现 WebDAV 同步功能。在架构方面，我深入理解了 MVVM 架构的优势，学会了如何将业务逻辑与 UI 分离，提高了代码的可维护性和可测试性。", font: "宋体", size: 24 })]
        }),
        new Paragraph({
          spacing: { before: 100, after: 100 },
          indent: { firstLine: 480 },
          children: [new TextRun({ text: "总的来说，这次课程设计让我收获颇丰，不仅提高了我的 Android 开发技能，也让我对软件工程的整个流程有了更深入的理解。我相信这些经验和技能将对我未来的学习和工作产生积极的影响。", font: "宋体", size: 24 })]
        })
      ]
    }
  ]
});

// 辅助函数：创建信息表格行
function createInfoRow(label, separator, value) {
  return new TableRow({
    children: [
      new TableCell({
        borders: { top: { style: BorderStyle.NONE }, bottom: { style: BorderStyle.NONE }, left: { style: BorderStyle.NONE }, right: { style: BorderStyle.NONE } },
        width: { size: 2000, type: WidthType.DXA },
        children: [new Paragraph({ alignment: AlignmentType.RIGHT, children: [new TextRun({ text: label, font: "宋体", size: 24, bold: true })] })]
      }),
      new TableCell({
        borders: { top: { style: BorderStyle.NONE }, bottom: { style: BorderStyle.NONE }, left: { style: BorderStyle.NONE }, right: { style: BorderStyle.NONE } },
        width: { size: 1000, type: WidthType.DXA },
        children: [new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: separator, font: "宋体", size: 24, bold: true })] })]
      }),
      new TableCell({
        borders: { top: { style: BorderStyle.NONE }, bottom: { style: BorderStyle.NONE }, left: { style: BorderStyle.NONE }, right: { style: BorderStyle.NONE } },
        width: { size: 6360, type: WidthType.DXA },
        children: [new Paragraph({ children: [new TextRun({ text: value, font: "宋体", size: 24 })] })]
      })
    ]
  });
}

// 辅助函数：创建表格行
function createTableRow(cells, isHeader = false) {
  return new TableRow({
    tableHeader: isHeader,
    children: cells.map(cell => 
      new TableCell({
        borders: { 
          top: { style: BorderStyle.SINGLE, size: 1, color: "000000" }, 
          bottom: { style: BorderStyle.SINGLE, size: 1, color: "000000" }, 
          left: { style: BorderStyle.SINGLE, size: 1, color: "000000" }, 
          right: { style: BorderStyle.SINGLE, size: 1, color: "000000" } 
        },
        width: { size: 2340, type: WidthType.DXA },
        shading: isHeader ? { fill: "D5E8F0", type: ShadingType.CLEAR } : undefined,
        children: [new Paragraph({ 
          alignment: isHeader ? AlignmentType.CENTER : AlignmentType.LEFT,
          children: [new TextRun({ 
            text: cell, 
            font: "宋体", 
            size: isHeader ? 22 : 20, 
            bold: isHeader 
          })]
        })]
      })
    )
  });
}

// 生成文档
Packer.toBuffer(doc).then(buffer => {
  fs.writeFileSync('计科3班-233817310311-程瑞琪.docx', buffer);
  console.log('文档生成成功！');
}).catch(err => {
  console.error('生成文档时出错：', err);
});