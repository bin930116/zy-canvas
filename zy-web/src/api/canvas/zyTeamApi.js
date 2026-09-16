import { baseRequest } from '@/utils/request'

const request = (url, ...arg) => baseRequest(`/canvas/team/` + url, ...arg)

/**
 * 团队信息表Api接口管理器
 *
 * @author hanbin
 * @date  2026/09/07 18:48
 **/
export default {
	// 获取团队信息表分页
	zyTeamPage(data) {
		return request('page', data, 'get')
	},
	// 提交团队信息表表单 edit为true时为编辑，默认为新增
	zyTeamSubmitForm(data, edit = false) {
		return request(edit ? 'edit' : 'add', data)
	},
	// 删除团队信息表
	zyTeamDelete(data) {
		return request('delete', data)
	},
	// 获取团队信息表详情
	zyTeamDetail(data) {
		return request('detail', data, 'get')
	},
	// 下载团队信息表导入模板
    zyTeamDownloadTemplate(data) {
		return request('downloadImportTemplate', data, 'get', {
			responseType: 'blob'
		})
    },
	// 导入团队信息表
	zyTeamImport(data) {
		return request('importData', data)
	},
	// 导出团队信息表
	zyTeamExport(data) {
		return request('exportData', data, 'post', {
			responseType: 'blob'
		})
	},
}
