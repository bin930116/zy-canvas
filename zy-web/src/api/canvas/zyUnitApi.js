import { baseRequest } from '@/utils/request'

const request = (url, ...arg) => baseRequest(`/canvas/unit/` + url, ...arg)

/**
 * 短剧项目章节Api接口管理器
 *
 * @author hanbin
 * @date  2026/09/07 18:37
 **/
export default {
	// 获取短剧项目章节分页
	zyUnitPage(data) {
		return request('page', data, 'get')
	},
	// 提交短剧项目章节表单 edit为true时为编辑，默认为新增
	zyUnitSubmitForm(data, edit = false) {
		return request(edit ? 'edit' : 'add', data)
	},
	// 删除短剧项目章节
	zyUnitDelete(data) {
		return request('delete', data)
	},
	// 获取短剧项目章节详情
	zyUnitDetail(data) {
		return request('detail', data, 'get')
	},
	// 下载短剧项目章节导入模板
    zyUnitDownloadTemplate(data) {
		return request('downloadImportTemplate', data, 'get', {
			responseType: 'blob'
		})
    },
	// 导入短剧项目章节
	zyUnitImport(data) {
		return request('importData', data)
	},
	// 导出短剧项目章节
	zyUnitExport(data) {
		return request('exportData', data, 'post', {
			responseType: 'blob'
		})
	},
}
