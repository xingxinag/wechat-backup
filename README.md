# WeChat Backup Module Announcements

这是微信数据备份模块的公告和更新服务器。

## 目录结构
wechat-backup-announcements/
├── api/
│ ├── announcement.json # 公告数据
│ ├── agreement.txt # 使用协议
│ └── version.json # 版本信息
└── README.md

## API 说明

### 1. 公告接口

**请求地址**: `/api/announcement.json`

返回最新的公告信息，包括更新通知、使用提醒等。

示例响应：
json
{
"announcements": [
{
"id": 3,
"title": "版本更新 1.1.0",
"content": "• 新增手动恢复功能\n• 优化备份速度\n• 修复已知问题",
"date": "2024-01-25",
"important": true,
"minVersion": "1.0.0",
"maxVersion": "1.1.0"
}
],
"settings": {
"checkInterval": 3600,
"forceUpdate": false
}
}

### 2. 版本信息接口

**请求地址**: `/api/version.json`

返回最新版本信息和历史版本记录。

示例响应：
json
{
"latest": {
"versionCode": 2,
"versionName": "1.1.0",
"url": "https://github.com/your-username/your-repo/releases/download/v1.1.0/app-release.apk",
"changelog": "• 新增手动恢复功能\n• 优化备份速度\n• 修复已知问题",
"forceUpdate": false,
"minSupport": "1.0.0"
}
}


### 3. 使用协议接口

**请求地址**: `/api/agreement.txt`

返回最新的使用协议文本。

## 更新说明

1. 所有更新都通过提交到 `main` 分支自动部署
2. 修改相应的 JSON 或 TXT 文件即可更新内容
3. 提交后会自动触发 GitHub Actions 进行部署

## 版本历史

### v1.1.0 (2024-01-25)
- 新增手动恢复功能
- 优化备份速度
- 修复已知问题

### v1.0.0 (2024-01-01)
- 首次发布
- 基础备份功能
- 基础恢复功能

## 维护说明

1. 更新公告时请确保 ID 递增
2. 版本号请遵循语义化版本规范
3. 重要更新请设置 `important: true`
4. 请保持更新日志的及时性和准确性

## 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

## 联系方式

如有问题请通过以下方式联系：

- Issues: [GitHub Issues](https://github.com/your-username/wechat-backup-announcements/issues)
- Email: your-email@example.com

## 贡献指南

1. Fork 本仓库
2. 创建新的分支
3. 提交您的更改
4. 发起 Pull Request

感谢您的贡献！
