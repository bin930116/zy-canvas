import { baseRequest } from '@/utils/request'

const request = (url, ...arg) => baseRequest(`/canvas/canvasunitlink/` + url, ...arg)

/**
 * 画布与章节关联Api接口管理器
 *
 * @author hanbin
 * @date  2026/09/08 14:31
 **/
export default {
	// 获取画布与章节关联分页
	zyCanvasUnitLinkPage(data) {
		return request('page', data, 'get')
	},
	// 提交画布与章节关联表单 edit为true时为编辑，默认为新增
	zyCanvasUnitLinkSubmitForm(data, edit = false) {
		return request(edit ? 'edit' : 'add', data)
	},
	// 删除画布与章节关联
	zyCanvasUnitLinkDelete(data) {
		return request('delete', data)
	},
	// 获取画布与章节关联详情
	zyCanvasUnitLinkDetail(data) {
		return request('detail', data, 'get')
	},
	// 下载画布与章节关联导入模板
    zyCanvasUnitLinkDownloadTemplate(data) {
		return request('downloadImportTemplate', data, 'get', {
			responseType: 'blob'
		})
    },
	// 导入画布与章节关联
	zyCanvasUnitLinkImport(data) {
		return request('importData', data)
	},
	// 导出画布与章节关联
	zyCanvasUnitLinkExport(data) {
		return request('exportData', data, 'post', {
			responseType: 'blob'
		})
	},
}
