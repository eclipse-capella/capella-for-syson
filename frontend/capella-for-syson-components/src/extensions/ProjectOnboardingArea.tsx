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
  MainAreaComponentProps,
  useComponents,
  workbenchMainAreaExtensionPoint,
} from '@eclipse-sirius/sirius-components-core';
import { useCurrentProject } from '@eclipse-sirius/sirius-web-application';
import { CapellaOnboardingArea } from './CapellaOnboardingArea';

const CAPELLA_NATURE = 'siriusWeb://nature?kind=capella';

// https://github.com/eclipse-sirius/sirius-web/issues/7000
export const ProjectOnboardingArea = ({ editingContextId, readOnly }: MainAreaComponentProps) => {
  const { project } = useCurrentProject();
  const siriusWebOnboardingArea = useComponents(workbenchMainAreaExtensionPoint).find(
    (component) => component.identifier === `siriusweb_${workbenchMainAreaExtensionPoint.identifier}`
  );

  if (project.natures.some(({ name }) => name === CAPELLA_NATURE)) {
    return <CapellaOnboardingArea editingContextId={editingContextId} readOnly={readOnly} />;
  }

  const SiriusWebOnboardingArea = siriusWebOnboardingArea?.Component;
  return SiriusWebOnboardingArea ? (
    <SiriusWebOnboardingArea editingContextId={editingContextId} readOnly={readOnly} />
  ) : null;
};
