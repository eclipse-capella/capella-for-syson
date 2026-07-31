/*******************************************************************************
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
 *******************************************************************************/

import {
  ComponentExtension,
  DataExtension,
  ExtensionRegistryMergeStrategy,
  workbenchMainAreaExtensionPoint,
  workbenchViewContributionExtensionPoint,
} from '@eclipse-sirius/sirius-components-core';
import { SysONExtensionRegistryMergeStrategy } from '@eclipse-syson/syson-components';

export class CapellaExtensionRegistryMergeStrategy
  extends SysONExtensionRegistryMergeStrategy
  implements ExtensionRegistryMergeStrategy
{
  public override mergeComponentExtensions(
    identifier: string,
    existingValues: ComponentExtension<any>[],
    newValues: ComponentExtension<any>[]
  ): ComponentExtension<any>[] {
    if (identifier === workbenchMainAreaExtensionPoint.identifier) {
      return [...newValues];
    }
    return super.mergeComponentExtensions(identifier, existingValues, newValues);
  }

  public override mergeDataExtensions(
    identifier: string,
    existingValues: DataExtension<any>,
    newValues: DataExtension<any>
  ): DataExtension<any> {
    if (identifier === workbenchViewContributionExtensionPoint.identifier) {
      return this.mergeWorkbenchViewContributions(existingValues, newValues);
    }
    return super.mergeDataExtensions(identifier, existingValues, newValues);
  }

  private mergeWorkbenchViewContributions(
    existingWorkbenchViewContributions: DataExtension<any>,
    newWorkbenchViewContributions: DataExtension<any>
  ): DataExtension<any> {
    return {
      identifier: `capella_${workbenchViewContributionExtensionPoint.identifier}`,
      data: [...existingWorkbenchViewContributions.data, ...newWorkbenchViewContributions.data],
    };
  }
}
