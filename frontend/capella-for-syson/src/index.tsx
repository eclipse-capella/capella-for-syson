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

import { loadDevMessages, loadErrorMessages } from '@apollo/client/dev';
import {
  ExtensionRegistry,
  workbenchMainAreaExtensionPoint,
  WorkbenchViewContribution,
  workbenchViewContributionExtensionPoint,
} from '@eclipse-sirius/sirius-components-core';
import { diagramToolbarActionExtensionPoint, NodeTypeContribution } from '@eclipse-sirius/sirius-components-diagrams';
import {
  GQLWidget,
  PropertySectionComponent,
  widgetContributionExtensionPoint,
} from '@eclipse-sirius/sirius-components-forms';
import {
  OmniboxCommand,
  OmniboxCommandOverrideContribution,
  omniboxCommandOverrideContributionExtensionPoint,
} from '@eclipse-sirius/sirius-components-omnibox';
import {
  GQLTool,
  PaletteToolOverriddenContributionProps,
  paletteToolOverrideExtensionPoint,
} from '@eclipse-sirius/sirius-components-palette';
import { TREE_REPRESENTATION_KIND } from '@eclipse-sirius/sirius-components-trees';
import {
  ApolloClientOptionsConfigurer,
  apolloClientOptionsConfigurersExtensionPoint,
  DiagramRepresentationConfiguration,
  ImportLibraryCommand,
  navigationBarIconExtensionPoint,
  navigationBarMenuHelpURLExtensionPoint,
  navigationBarMenuIconExtensionPoint,
  NodeTypeRegistry,
  SiriusWebApplication,
} from '@eclipse-sirius/sirius-web-application';
import {
  ExpressionPropertySection,
  InsertTextualSysMLv2ExplorerToolOverriddenContribution,
  PublishProjectSysMLContentsAsLibraryCommand,
  SysMLImportedPackageNode,
  SysMLImportedPackageNodeConverter,
  SysMLImportedPackageNodeLayoutHandler,
  sysMLNodesStyleDocumentTransform,
  SysMLNoteNode,
  SysMLNoteNodeConverter,
  SysMLNoteNodeLayoutHandler,
  SysMLPackageNode,
  SysMLPackageNodeConverter,
  SysMLPackageNodeLayoutHandler,
  SysMLViewFrameNode,
  SysMLViewFrameNodeConverter,
  SysMLViewFrameNodeLayoutHandler,
  SysONNavigationBarMenuIcon,
} from '@eclipse-syson/syson-components';
import BubbleChartIcon from '@mui/icons-material/BubbleChart';
import QuestionMarkOutlinedIcon from '@mui/icons-material/QuestionMarkOutlined';
import {
  CapellaDDVWorkbenchViewContribution,
  CapellaDiagramPanelMenu,
  CapellaExtensionRegistryMergeStrategy,
  CapellaOnboardArea,
} from '@obeo/capella-for-syson-components';
import { createRoot } from 'react-dom/client';
import { httpOrigin, wsOrigin } from './core/URL';
import { CapellaNavigationBarIcon } from './extensions/CapellaNavigationBarIcon';
import './fonts.css';
import './ReactFlow.css';
import { capellaTheme } from './theme/capellaTheme';
import './variables.css';

if (process.env.NODE_ENV !== 'production') {
  loadDevMessages();
  loadErrorMessages();
}

const extensionRegistry: ExtensionRegistry = new ExtensionRegistry();

extensionRegistry.addComponent(navigationBarIconExtensionPoint, {
  identifier: `capella_${navigationBarIconExtensionPoint.identifier}`,
  Component: CapellaNavigationBarIcon,
});
extensionRegistry.putData(navigationBarMenuHelpURLExtensionPoint, {
  identifier: `capella_${navigationBarMenuHelpURLExtensionPoint.identifier}`,
  data: 'https://mbse-capella.org',
});

const omniboxCommandOverrides: OmniboxCommandOverrideContribution[] = [
  {
    canHandle: (action: OmniboxCommand) => {
      return action.id === 'publishProjectSysMLContentsAsLibrary';
    },
    component: PublishProjectSysMLContentsAsLibraryCommand,
  },
  {
    canHandle: (action: OmniboxCommand) => {
      return action.id === 'importPublishedLibrary';
    },
    component: ImportLibraryCommand,
  },
];

extensionRegistry.putData<OmniboxCommandOverrideContribution[]>(omniboxCommandOverrideContributionExtensionPoint, {
  identifier: `syson_${omniboxCommandOverrideContributionExtensionPoint.identifier}`,
  data: omniboxCommandOverrides,
});

const apolloClientOptionsConfigurer: ApolloClientOptionsConfigurer = (currentOptions) => {
  const { documentTransform } = currentOptions;

  const newDocumentTransform = documentTransform
    ? documentTransform.concat(sysMLNodesStyleDocumentTransform)
    : sysMLNodesStyleDocumentTransform;
  return {
    ...currentOptions,
    documentTransform: newDocumentTransform,
  };
};
extensionRegistry.putData(apolloClientOptionsConfigurersExtensionPoint, {
  identifier: `syson_${apolloClientOptionsConfigurersExtensionPoint.identifier}`,
  data: [apolloClientOptionsConfigurer],
});

extensionRegistry.addComponent(diagramToolbarActionExtensionPoint, {
  identifier: `capella_${diagramToolbarActionExtensionPoint.identifier}_CustomPanelEntriesMenu`,
  Component: CapellaDiagramPanelMenu,
});

extensionRegistry.addComponent(navigationBarMenuIconExtensionPoint, {
  identifier: `syson_${navigationBarMenuIconExtensionPoint.identifier}`,
  Component: SysONNavigationBarMenuIcon,
});

const paletteToolOverriddenContributions: PaletteToolOverriddenContributionProps[] = [
  {
    canHandle: (representationDescriptionId: string, tool: GQLTool) => {
      return representationDescriptionId === TREE_REPRESENTATION_KIND && tool.id === 'newObjectsFromText';
    },
    component: InsertTextualSysMLv2ExplorerToolOverriddenContribution,
  },
];

extensionRegistry.putData<PaletteToolOverriddenContributionProps[]>(paletteToolOverrideExtensionPoint, {
  identifier: `syson_${paletteToolOverrideExtensionPoint.identifier}`,
  data: paletteToolOverriddenContributions,
});

extensionRegistry.addComponent(workbenchMainAreaExtensionPoint, {
  identifier: `capella_${workbenchMainAreaExtensionPoint.identifier}`,
  Component: CapellaOnboardArea,
});

extensionRegistry.putData(widgetContributionExtensionPoint, {
  identifier: `syson_${widgetContributionExtensionPoint.identifier}`,
  data: [
    {
      name: 'ExpressionValuePropertySectionOverride',
      icon: <QuestionMarkOutlinedIcon />,
      previewComponent: () => null,
      component: (widget: GQLWidget): PropertySectionComponent<GQLWidget> | null => {
        let propertySectionComponent: PropertySectionComponent<GQLWidget> | null = null;
        if (
          widget.__typename == 'LabelWidget' &&
          (widget.label.startsWith('syson:expression-value-widget') ||
            widget.label.startsWith('syson:missing-expression-value-widget'))
        ) {
          propertySectionComponent = ExpressionPropertySection as PropertySectionComponent<GQLWidget>;
        }
        return propertySectionComponent;
      },
    },
  ],
});

/*******************************************************************************
 *
 * WorkbenchView contributions
 *
 * Used to contribute new views to the workbench
 *
 *******************************************************************************/

const workbenchViewContributions: WorkbenchViewContribution[] = [
  {
    id: 'ddv-view',
    title: 'Related Elements Visual View',
    icon: <BubbleChartIcon />,
    component: CapellaDDVWorkbenchViewContribution,
  },
];

extensionRegistry.putData(workbenchViewContributionExtensionPoint, {
  identifier: `capella_${workbenchViewContributionExtensionPoint.identifier}`,
  data: workbenchViewContributions,
});

/*
 * Custom node contribution
 */
const nodeTypeRegistry: NodeTypeRegistry = {
  nodeLayoutHandlers: [
    new SysMLPackageNodeLayoutHandler(),
    new SysMLNoteNodeLayoutHandler(),
    new SysMLImportedPackageNodeLayoutHandler(),
    new SysMLViewFrameNodeLayoutHandler(),
  ],
  nodeConverters: [
    new SysMLPackageNodeConverter(),
    new SysMLNoteNodeConverter(),
    new SysMLImportedPackageNodeConverter(),
    new SysMLViewFrameNodeConverter(),
  ],
  nodeTypeContributions: [
    <NodeTypeContribution component={SysMLPackageNode} type={'sysMLPackageNode'} />,
    <NodeTypeContribution component={SysMLNoteNode} type={'sysMLNoteNode'} />,
    <NodeTypeContribution component={SysMLImportedPackageNode} type={'sysMLImportedPackageNode'} />,
    <NodeTypeContribution component={SysMLViewFrameNode} type={'sysMLViewFrameNode'} />,
  ],
};

const container = document.getElementById('root');
const root = createRoot(container!);
root.render(
  <SiriusWebApplication
    httpOrigin={httpOrigin}
    wsOrigin={wsOrigin}
    theme={capellaTheme}
    extensionRegistryMergeStrategy={new CapellaExtensionRegistryMergeStrategy()}
    extensionRegistry={extensionRegistry}>
    <DiagramRepresentationConfiguration nodeTypeRegistry={nodeTypeRegistry} />
  </SiriusWebApplication>
);
