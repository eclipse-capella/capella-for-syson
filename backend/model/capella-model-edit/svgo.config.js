/*
 * Copyright (c) 2026 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 */
module.exports = {
	js2svg: {
		indent: 4, // number
		pretty: true, // boolean
	},
	plugins: [
		{
			name: 'preset-default',
			params: {
				overrides: {

					// customize the params of a default plugin
					inlineStyles: {
						onlyMatchedOnce: false,
					},
				},
			},
		},
	]
};
