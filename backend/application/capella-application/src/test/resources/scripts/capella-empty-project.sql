INSERT INTO semantic_data (id, created_on, last_modified_on)
VALUES ('a8f1f85b-6e6d-4b35-9a9d-9c69ec1d4006', '2026-01-01 00:00:00+00', '2026-01-01 00:00:00+00');

INSERT INTO semantic_data_domain (semantic_data_id, uri)
VALUES ('a8f1f85b-6e6d-4b35-9a9d-9c69ec1d4006', 'http://www.eclipse.org/syson/sysml');

INSERT INTO document (id, semantic_data_id, name, content, is_read_only, created_on, last_modified_on)
VALUES (
    'fd17cf24-44c0-4770-a4c0-f9445b77f124',
    'a8f1f85b-6e6d-4b35-9a9d-9c69ec1d4006',
    'Capella.sysml',
    $$
{
  "json": {
    "version": "1.0",
    "encoding": "utf-8"
  },
  "ns": {
    "sysml": "http://www.eclipse.org/syson/sysml"
  },
  "content": [
    {
      "id": "1e1105b6-7cd3-4679-9fb4-0f6659f82cb8",
      "eClass": "sysml:Namespace",
      "data": {
        "eAnnotations": [
          {
            "source": "org.eclipse.syson.sysml.imported"
          }
        ],
        "elementId": "5ffd10fa-8153-4f16-9bac-b00a2a9c9d2c",
        "ownedRelationship": [
          {
            "id": "6915e8e0-310d-4c71-b2f5-5399a54e45f7",
            "eClass": "sysml:OwningMembership",
            "data": {
              "elementId": "e938f20d-30a8-496d-9ee1-10d4d3731e7a",
              "ownedRelatedElement": [
                {
                  "id": "5dc36abf-abaa-415d-8781-ddf29777da33",
                  "eClass": "sysml:OccurrenceDefinition",
                  "data": {
                    "elementId": "b18b805b-2cc4-4a81-86ae-d61c6543be64",
                    "ownedRelationship": [
                      {
                        "id": "e619e065-32a4-4d34-bdda-45b4aba32947",
                        "eClass": "sysml:OwningMembership",
                        "data": {
                          "elementId": "28c12052-a767-4755-b976-eff03e172e5c",
                          "ownedRelatedElement": [
                            {
                              "id": "5aecbea1-5131-4fd4-b0f3-9f76f705564b",
                              "eClass": "sysml:MetadataUsage",
                              "data": {
                                "elementId": "cb938d8b-f19d-44a8-9e7b-e03e784054b9",
                                "ownedRelationship": [
                                  {
                                    "id": "8a9c8675-2d6e-40a0-92cc-4535343aed9b",
                                    "eClass": "sysml:FeatureMembership",
                                    "data": {
                                      "elementId": "f166d6df-7610-458a-9481-3fbb1169f497",
                                      "ownedRelatedElement": [
                                        {
                                          "id": "672cb67b-5be3-43b1-ac7c-3504229ebb2f",
                                          "eClass": "sysml:ReferenceUsage",
                                          "data": {
                                            "elementId": "3d40a285-5cb7-49a9-a03c-60aa88512634",
                                            "ownedRelationship": [
                                              {
                                                "id": "0f3659a7-e7cb-4fb6-8fde-88d24409b93b",
                                                "eClass": "sysml:FeatureValue",
                                                "data": {
                                                  "elementId": "3f9a412f-b2c7-4ca8-9d70-cf1fba7b73cc",
                                                  "ownedRelatedElement": [
                                                    {
                                                      "id": "9887fe5a-7ed3-48e9-aa5f-2af2677f6f5f",
                                                      "eClass": "sysml:FeatureReferenceExpression",
                                                      "data": {
                                                        "elementId": "5b6ebe6a-417c-4c3f-b7f6-6363b1b8d96b",
                                                        "ownedRelationship": [
                                                          {
                                                            "id": "6e6da0e0-d7ed-4b7a-96df-5e936bd095aa",
                                                            "eClass": "sysml:Membership",
                                                            "data": {
                                                              "elementId": "916d1d41-6087-4352-8dff-db023d29937f",
                                                              "memberElement": "sysml:EnumerationUsage sirius:///0fbf595a-7e8d-3814-a11b-12ba3223e7f7#16bb4a92-9484-4d0c-b2c1-7b410df49c7b"
                                                            }
                                                          }
                                                        ]
                                                      }
                                                    }
                                                  ]
                                                }
                                              },
                                              {
                                                "id": "e4dd0d72-a3cb-4b24-bb3e-c4f4c418a4c7",
                                                "eClass": "sysml:Redefinition",
                                                "data": {
                                                  "elementId": "0c611ce7-46ec-4a89-b81d-6980aa031c6f",
                                                  "redefinedFeature": "sysml:EnumerationUsage sirius:///0fbf595a-7e8d-3814-a11b-12ba3223e7f7#e69df7d5-61e8-4c22-8d6f-ac0442947b7c",
                                                  "redefiningFeature": "672cb67b-5be3-43b1-ac7c-3504229ebb2f"
                                                }
                                              }
                                            ],
                                            "isComposite": true
                                          }
                                        }
                                      ]
                                    }
                                  },
                                  {
                                    "id": "7b0cf1a9-ade5-47df-adea-80519c25efb2",
                                    "eClass": "sysml:FeatureTyping",
                                    "data": {
                                      "elementId": "94830fa9-fc04-4016-baf6-f73ef4445f5a",
                                      "type": "sysml:MetadataDefinition sirius:///0fbf595a-7e8d-3814-a11b-12ba3223e7f7#37a6850e-bec4-483c-9e4c-dd46d8bb9e9c",
                                      "typedFeature": "5aecbea1-5131-4fd4-b0f3-9f76f705564b"
                                    }
                                  }
                                ],
                                "isComposite": true
                              }
                            }
                          ]
                        }
                      },
                      {
                        "id": "d76b0d0e-954c-4772-a310-286e5548550f",
                        "eClass": "sysml:NamespaceImport",
                        "data": {
                          "elementId": "089bef31-fee6-409f-a155-baec3d98ad39",
                          "importedNamespace": "sysml:LibraryPackage sirius:///0fbf595a-7e8d-3814-a11b-12ba3223e7f7#f60f8fef-cbd2-4e1d-8fd4-48cedaadd6f7"
                        }
                      },
                      {
                        "id": "0fb9061e-a6f5-420b-aa6c-1bffeb8cbfbe",
                        "eClass": "sysml:MembershipImport",
                        "data": {
                          "elementId": "45440171-ba7a-4676-8647-43898bbb8196",
                          "importedMembership": "sysml:OwningMembership kermllibrary:///b2c6dd37-2084-3ce4-9ce2-580fdf30629c#770c6929-2eb2-5c03-aaff-570ce03a47b5"
                        }
                      },
                      {
                        "id": "539d50dc-cd17-4ed1-97a5-a96ce68a8b63",
                        "eClass": "sysml:NamespaceImport",
                        "data": {
                          "elementId": "7442250d-e066-443a-a05e-7381699ae244",
                          "importedNamespace": "sysml:LibraryPackage sysmllibrary:///6f8a7309-c6fc-3fb6-ac6f-e624d77d6ba7#a47452c7-dd0b-5f88-8a7f-dc6ee889ed29"
                        }
                      },
                      {
                        "id": "1865b6b6-b243-45ec-a371-929f1616f91b",
                        "eClass": "sysml:MembershipImport",
                        "data": {
                          "elementId": "82970c7b-342a-41f4-b109-e88edf876d2a",
                          "importedMembership": "sysml:OwningMembership kermllibrary:///e85548ea-3303-3b94-9a96-00f56f55c7e4#1dd62552-01ed-5164-8e92-92ea4c23f370"
                        }
                      },
                      {
                        "id": "d231737d-52b4-487c-8994-0b61fe806a05",
                        "eClass": "sysml:OwningMembership",
                        "data": {
                          "elementId": "4b29a199-0a15-4d6e-acca-12b537227bdf",
                          "ownedRelatedElement": [
                            {
                              "id": "64c52d0a-8e4d-4ca1-8ced-81dbe8257bfd",
                              "eClass": "sysml:Package",
                              "data": {
                                "declaredName": "Operational Analysis",
                                "elementId": "73b91e49-f69f-4296-a084-211317c58665",
                                "ownedRelationship": [
                                  {
                                    "id": "f2852617-ec21-4788-a681-db0506dc929e",
                                    "eClass": "sysml:OwningMembership",
                                    "data": {
                                      "elementId": "afdaa18e-5e91-451c-b8ae-8c45f9d59b62",
                                      "ownedRelatedElement": [
                                        {
                                          "id": "2c0356db-7873-4418-9695-25d464cad4f8",
                                          "eClass": "sysml:Package",
                                          "data": {
                                            "declaredName": "Structure",
                                            "elementId": "e01b6b39-576a-4ee8-9f00-f6debb68c991",
                                            "ownedRelationship": [
                                              {
                                                "id": "48f5b1d3-3dd3-4e7c-9670-32561553ce00",
                                                "eClass": "sysml:NamespaceImport",
                                                "data": {
                                                  "elementId": "16924b68-236c-4d4b-bb13-c1904fdcfaa7",
                                                  "importedNamespace": "bd7a2c78-ac97-435a-b2e6-3ebe097a0c7a"
                                                }
                                              },
                                              {
                                                "id": "f243de7f-dd5e-4745-b944-139a1a1c1fc7",
                                                "eClass": "sysml:NamespaceImport",
                                                "data": {
                                                  "elementId": "1408a9c7-f1b0-4f96-b644-590cb251cb4b",
                                                  "importedNamespace": "ab142a11-730e-4149-a710-c21dd724721e"
                                                }
                                              }
                                            ]
                                          }
                                        }
                                      ]
                                    }
                                  },
                                  {
                                    "id": "c0e1fe7c-fc60-4da1-8ddb-93bceb930e09",
                                    "eClass": "sysml:OwningMembership",
                                    "data": {
                                      "elementId": "8227da3f-64ed-4ff0-a705-2c0aca636b00",
                                      "ownedRelatedElement": [
                                        {
                                          "id": "434d4d45-b436-4cf4-a105-907c89d5e251",
                                          "eClass": "sysml:Package",
                                          "data": {
                                            "declaredName": "Capabilities",
                                            "elementId": "c5f10243-5a76-4bc1-b81b-ceb7233b8c00",
                                            "ownedRelationship": [
                                              {
                                                "id": "e386d33f-b5e9-4c91-8769-20991238fbd2",
                                                "eClass": "sysml:NamespaceImport",
                                                "data": {
                                                  "elementId": "12f7b287-6a22-4363-a9a4-25c331b9a617",
                                                  "importedNamespace": "bd7a2c78-ac97-435a-b2e6-3ebe097a0c7a"
                                                }
                                              },
                                              {
                                                "id": "4d19aec4-2d51-4708-af87-e97d71d2ede2",
                                                "eClass": "sysml:NamespaceImport",
                                                "data": {
                                                  "elementId": "2ab5adb3-3973-4c95-92ea-6744bfb8af95",
                                                  "importedNamespace": "2c0356db-7873-4418-9695-25d464cad4f8"
                                                }
                                              }
                                            ]
                                          }
                                        }
                                      ]
                                    }
                                  }
                                ]
                              }
                            }
                          ]
                        }
                      },
                      {
                        "id": "81447d5d-b4ad-44cf-938c-a9241ae30268",
                        "eClass": "sysml:OwningMembership",
                        "data": {
                          "elementId": "a40d1218-498b-44f7-8873-b99059be47c0",
                          "ownedRelatedElement": [
                            {
                              "id": "69271ba7-9d78-4e60-a93d-8ae5ea1df693",
                              "eClass": "sysml:Package",
                              "data": {
                                "declaredName": "System Analysis",
                                "elementId": "e57e947c-449c-4ac3-b711-4d869abbd2e0",
                                "ownedRelationship": [
                                  {
                                    "id": "943fe0cc-9247-4432-a319-a4eaaeefe177",
                                    "eClass": "sysml:OwningMembership",
                                    "data": {
                                      "elementId": "fadc29ef-1e59-4bd0-999f-39d74e14b872",
                                      "ownedRelatedElement": [
                                        {
                                          "id": "a00fc07c-bc6e-499b-ac3d-f2598ab505f0",
                                          "eClass": "sysml:Package",
                                          "data": {
                                            "declaredName": "Structure",
                                            "elementId": "6a599f76-419e-4464-9ce0-bb3ce3c710c3",
                                            "ownedRelationship": [
                                              {
                                                "id": "0fad37eb-ea85-494f-8036-4014578a955e",
                                                "eClass": "sysml:NamespaceImport",
                                                "data": {
                                                  "elementId": "cf58aefa-70c6-4013-aee7-861c6a3851aa",
                                                  "importedNamespace": "55340c06-26de-415e-8e3a-db31dcf8ff03"
                                                }
                                              },
                                              {
                                                "id": "e85d6509-cfa1-4ffb-b04b-73d80426929d",
                                                "eClass": "sysml:NamespaceImport",
                                                "data": {
                                                  "elementId": "70935303-7e41-48dd-98f7-d06073d1a22f",
                                                  "importedNamespace": "26905e9d-f33c-47a3-aebc-190b26038410"
                                                }
                                              },
                                              {
                                                "id": "25d19c43-dc63-4792-9843-3a3812710e1d",
                                                "eClass": "sysml:OwningMembership",
                                                "data": {
                                                  "elementId": "1cb83fb6-3498-4c16-873f-0d7be2cb8a64",
                                                  "ownedRelatedElement": [
                                                    {
                                                      "id": "873cc493-1aef-4483-9422-f330fcf51a07",
                                                      "eClass": "sysml:PartUsage",
                                                      "data": {
                                                        "declaredName": "system",
                                                        "elementId": "3448f4fe-9f0c-4fc5-b6eb-61debad9a87f",
                                                        "ownedRelationship": [
                                                          {
                                                            "id": "8e853f90-475d-4c9c-b9e8-28aa24f7f535",
                                                            "eClass": "sysml:FeatureTyping",
                                                            "data": {
                                                              "elementId": "1a876b57-d3dc-47bb-9b36-62ef3ba957d7",
                                                              "type": "sysml:PartDefinition sirius:///0fbf595a-7e8d-3814-a11b-12ba3223e7f7#0bab9a5b-37e1-48a7-a66b-42594664e3a9",
                                                              "typedFeature": "873cc493-1aef-4483-9422-f330fcf51a07"
                                                            }
                                                          }
                                                        ],
                                                        "isComposite": true
                                                      }
                                                    }
                                                  ]
                                                }
                                              }
                                            ]
                                          }
                                        }
                                      ]
                                    }
                                  }
                                ]
                              }
                            }
                          ]
                        }
                      },
                      {
                        "id": "0e99c5b9-aaa0-4ed7-9ab3-56e42ed6a178",
                        "eClass": "sysml:OwningMembership",
                        "data": {
                          "elementId": "b6904d90-470c-47ae-ac57-9a97816b5f79",
                          "ownedRelatedElement": [
                            {
                              "id": "8be07ef3-12a8-4a85-921e-f3d3f23dac0d",
                              "eClass": "sysml:Package",
                              "data": {
                                "declaredName": "Logical Architecture",
                                "elementId": "8b4627fb-e17a-46ff-b26b-f89c08dc0f95",
                                "ownedRelationship": [
                                  {
                                    "id": "435a5988-3d62-48ee-b2ab-85e40e7b15ec",
                                    "eClass": "sysml:OwningMembership",
                                    "data": {
                                      "elementId": "74774259-6ee9-4899-ad5c-bd1c01df9474",
                                      "ownedRelatedElement": [
                                        {
                                          "id": "d6fe85e1-db06-430f-9812-c74e26d0a711",
                                          "eClass": "sysml:Package",
                                          "data": {
                                            "declaredName": "Structure",
                                            "elementId": "95e5117e-2a3d-4810-a5ee-8fbf01185fad",
                                            "ownedRelationship": [
                                              {
                                                "id": "d42e75e1-b30f-4cba-b159-c53c3a2c54e7",
                                                "eClass": "sysml:NamespaceImport",
                                                "data": {
                                                  "elementId": "d93eba13-3486-46a1-826c-47a64a611a46",
                                                  "importedNamespace": "8ee1f432-4ab0-4149-87ce-35e6bd9610ff"
                                                }
                                              },
                                              {
                                                "id": "05077db9-121a-4237-820c-77444c66c675",
                                                "eClass": "sysml:NamespaceImport",
                                                "data": {
                                                  "elementId": "50753428-80f7-43a7-b94b-9467d7330f00",
                                                  "importedNamespace": "50ccfafd-2545-4675-ba22-d16f6777c43a"
                                                }
                                              },
                                              {
                                                "id": "7b7648ef-b3a3-431b-b82d-d0ac06b6791c",
                                                "eClass": "sysml:OwningMembership",
                                                "data": {
                                                  "elementId": "365d61c7-54c1-4665-ab9f-c25d973bc918",
                                                  "ownedRelatedElement": [
                                                    {
                                                      "id": "992457d7-0c26-49dc-a2d8-e64a523de7b5",
                                                      "eClass": "sysml:PartUsage",
                                                      "data": {
                                                        "declaredName": "system",
                                                        "elementId": "18357b8a-3c93-402e-86fa-dee669913183",
                                                        "ownedRelationship": [
                                                          {
                                                            "id": "79480a15-3a55-4dcd-b2f5-3fdba6321376",
                                                            "eClass": "sysml:FeatureTyping",
                                                            "data": {
                                                              "elementId": "d8608ce1-4e11-4285-8724-cd174d5d2376",
                                                              "type": "sysml:PartDefinition sirius:///0fbf595a-7e8d-3814-a11b-12ba3223e7f7#0bab9a5b-37e1-48a7-a66b-42594664e3a9",
                                                              "typedFeature": "992457d7-0c26-49dc-a2d8-e64a523de7b5"
                                                            }
                                                          }
                                                        ],
                                                        "isComposite": true
                                                      }
                                                    }
                                                  ]
                                                }
                                              }
                                            ]
                                          }
                                        }
                                      ]
                                    }
                                  }
                                ]
                              }
                            }
                          ]
                        }
                      }
                    ]
                  }
                }
              ]
            }
          }
        ]
      }
    }
  ]
}
$$,
    false,
    '2026-01-01 00:00:00+00',
    '2026-01-01 00:00:00+00'
);

INSERT INTO project (id, name, created_on, last_modified_on)
VALUES ('2b0d2d8a-4cc9-48a6-9137-b1f879d6f49c', 'Capella empty project', '2026-01-01 00:00:00+00', '2026-01-01 00:00:00+00');

INSERT INTO nature (project_id, name)
VALUES ('2b0d2d8a-4cc9-48a6-9137-b1f879d6f49c', 'siriusWeb://nature?kind=capella');

INSERT INTO project_semantic_data (id, project_id, semantic_data_id, name, created_on, last_modified_on)
VALUES ('9b74e1c3-aa1d-43d4-a6ff-f750a2fe342b', '2b0d2d8a-4cc9-48a6-9137-b1f879d6f49c', 'a8f1f85b-6e6d-4b35-9a9d-9c69ec1d4006', 'main', '2026-01-01 00:00:00+00', '2026-01-01 00:00:00+00');
