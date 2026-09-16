import { baseRequest } from '@/utils/request'

const request = (url, ...arg) => baseRequest(`/canvas/teammember/` + url, ...arg)

/**
 * 团队成员关系表Api接口管理器
 *
 * @author hanbin
 * @date  2026/09/07 18:44
 **/
export default {
	// 获取团队成员关系表分页
	zyTeamMemberPage(data) {
		return request('page', data, 'get')
	},
	// 提交团队成员关系表表单 edit为true时为编辑，默认为新增
	zyTeamMemberSubmitForm(data, edit = false) {
		return request(edit ? 'edit' : 'add', data)
	},
	// 删除团队成员关系表
	zyTeamMemberDelete(data) {
		return request('delete', data)
	},
	// 获取团队成员关系表详情
	zyTeamMemberDetail(data) {
		return request('detail', data, 'get')
	},
	// 下载团队成员关系表导入模板
    zyTeamMemberDownloadTemplate(data) {
		return request('downloadImportTemplate', data, 'get', {
			responseType: 'blob'
		})
    },
	// 导入团队成员关系表
	zyTeamMemberImport(data) {
		return request('importData', data)
	},
	// 导出团队成员关系表
	zyTeamMemberExport(data) {
		return request('exportData', data, 'post', {
			responseType: 'blob'
		})
	},
}
